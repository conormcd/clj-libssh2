(ns clj-libssh2.known-hosts
  "Utilities for checking the host key of a remote machine against a list of
   known hosts."
  (:require [clojure.java.io :refer [file]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [digest]
            [clj-libssh2.error :as error :refer [handle-errors with-timeout]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.knownhost :as libssh2-knownhost]
            [clj-libssh2.libssh2.session :as libssh2-session])
  (:import [com.sun.jna.ptr IntByReference PointerByReference]))

(defn- checkp-result
  "Re-interpret the result of libssh2_knownhost_checkp to either succeed or
   cause handle-errors to throw, as appropriate.

   Arguments:

   fail-on-mismatch Boolean, true if a call to check-host-key should fail if
                    the remote host's key does not match the value in the known
                    hosts file.
   fail-on-missing  Boolean, true if a call to check-host-key should fail if
                    there is no entry for the remote host in the known hosts
                    file.
   result           The result of the call to libssh2_knownhost_checkp.

   Return:

   An integer, 0 if libssh2_knownhost_checkp was successful,
   libssh2/ERROR_HOSTKEY_SIGN if libssh2_knownhost_checkp was unsuccessful and
   that lack of success - interpreted in the context of fail-on-missing and
   fail-on-mismatch - is considered a failure. If libssh2_knownhost_checkp
   returns an unknown value then an exception will be raised."
  [fail-on-mismatch fail-on-missing result]
  (condp = (.longValue result)
    libssh2/KNOWNHOST_CHECK_MATCH     0
    libssh2/KNOWNHOST_CHECK_MISMATCH  (if fail-on-mismatch
                                        libssh2/ERROR_HOSTKEY_SIGN
                                        0)
    libssh2/KNOWNHOST_CHECK_NOTFOUND  (if fail-on-missing
                                        libssh2/ERROR_HOSTKEY_SIGN
                                        0)
    libssh2/KNOWNHOST_CHECK_FAILURE   libssh2/ERROR_HOSTKEY_SIGN
    (error/raise "Unknown return code from libssh2_knownhost_checkp."
                 {:function "libssh2_knownhost_checkp"
                  :return result})))

(defn- host-key
  "Get the remote host's key.

   Arguments:

   session The clj-libssh2.session.Session object for the current session.

   Return:

   A byte array containing the key of the remote host."
  [session]
  (when (:session session)
    (let [len (IntByReference.)
          typ (IntByReference.)
          hostkey_ptr (libssh2-session/hostkey (:session session) len typ)]
      (.getByteArray hostkey_ptr 0 (.getValue len)))))

(defn- check-host-key
  "Call libssh2_knownhost_checkp to check the current remote host.

   Arguments:

   session          The clj-libssh2.session.Session object for the current
                    session.
   known-hosts      A native pointer from libssh2_knownhost_init.
   host             The hostname or IP of the remote host.
   port             The port in use for connecting to the remote host.
   host-key         The remote host's key.
   fail-on-missing  Fail if the remote host's key is not in the known hosts
                    file.
   fail-on-mismatch Fail if the remote host's key does not match the one found
                    in the known hosts file.

   Return:

   0 on success or an exception if the key does not validate."
  [session known-hosts host port host-key fail-on-missing fail-on-mismatch]
  (handle-errors session
    (with-timeout session :known-hosts
      (checkp-result fail-on-mismatch fail-on-missing
        (libssh2-knownhost/checkp known-hosts
                                  host
                                  port
                                  host-key
                                  (count host-key)
                                  (bit-or libssh2/KNOWNHOST_TYPE_PLAIN
                                          libssh2/KNOWNHOST_KEYENC_RAW)
                                  (PointerByReference.))))))

(defn- load-known-hosts
  "Load a known hosts file into the known hosts object.

   Arguments:

   session          The clj-libssh2.session.Session object for the current
                    session.
   known-hosts      A native pointer from libssh2_knownhost_init.
   known-hosts-file The path to the file containing the known hosts.

   Return:

   Nil if the known hosts file did not exist. If the file does exist, this
   returns the number of hosts loaded. Throws an exception on error."
  [session known-hosts known-hosts-file]
  (when (.exists (file known-hosts-file))
    (handle-errors session
      (with-timeout session :known-hosts
        (libssh2-knownhost/readfile known-hosts
                                    known-hosts-file
                                    libssh2/KNOWNHOST_FILE_OPENSSH)))))

(defn- fingerprint
  "Generate a fingerprint for a host key.

   Arguments:

   host-key The full host key as a byte array.

   Return:

   A String with a fingerprint of the host key."
  [host-key]
  (->> host-key
       digest/sha-256
       (partition 2)
       (map #(str/join "" %))
       (str/join ":")))

(defn check
  "Given a session that has already completed a handshake with a remote host,
   check the host key of the remote host against the known hosts file.

   Arguments:

   session The clj-libssh2.session.Session object for the current session.

   Return:

   0 on success or an exception if the host key does not validate."
  [session]
  (let [known-hosts (libssh2-knownhost/init (:session session))
        remote-host-key (host-key session)
        session-options (:options session)
        file (or (:known-hosts-file session-options)
                 (str (System/getProperty "user.home") "/.ssh/known_hosts"))
        fail-on-mismatch (-> session-options :fail-unless-known-hosts-matches)
        fail-on-missing (-> session-options :fail-if-not-in-known-hosts)]
    (when (nil? known-hosts)
      (error/maybe-throw-error session libssh2/ERROR_ALLOC))
    (try
      (log/infof "Loading known hosts file: %s" file)
      (load-known-hosts session known-hosts file)
      (log/infof "Checking host key (%s) against known hosts file."
                 (fingerprint remote-host-key))
      (check-host-key session
                      known-hosts
                      (:host session)
                      (:port session)
                      remote-host-key
                      fail-on-missing
                      fail-on-mismatch)
      (finally (libssh2-knownhost/free known-hosts)))))
