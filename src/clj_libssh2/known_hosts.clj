(ns clj-libssh2.known-hosts
  (:require [clojure.java.io :refer [file]]
            [clj-libssh2.error :refer [handle-errors with-timeout]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.knownhost :as libssh2-knownhost]
            [clj-libssh2.libssh2.session :as libssh2-session])
  (:import [com.sun.jna.ptr IntByReference PointerByReference]))

(defn- checkp-result
  "Re-interpret the result of libssh2_knownhost_checkp to either succeed or
   cause handle-errors to throw, as appropriate."
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
    (throw (Exception. (format "Unknown return code from libssh2-knownhost/checkp: %d" result)))))

(defn- host-fingerprint
  "Get the remote host's fingerprint."
  [session]
  (when (:session session)
    (let [len (IntByReference.)
          typ (IntByReference.)
          hostkey_ptr (libssh2-session/hostkey (:session session) len typ)]
      (.getByteArray hostkey_ptr 0 (.getValue len)))))

(defn- check-fingerprint
  "Call libssh2_knownhost_checkp."
  [session known-hosts host port fingerprint fail-on-missing fail-on-mismatch]
  (handle-errors session
    (with-timeout :known-hosts
      (checkp-result fail-on-mismatch fail-on-missing
        (libssh2-knownhost/checkp known-hosts
                                  host
                                  port
                                  fingerprint
                                  (count fingerprint)
                                  (bit-or libssh2/KNOWNHOST_TYPE_PLAIN
                                          libssh2/KNOWNHOST_KEYENC_RAW)
                                  (PointerByReference.))))))

(defn- load-known-hosts
  "Load a known hosts file into the known hosts object."
  [session known-hosts known-hosts-file]
  (when (.exists (file known-hosts-file))
    (handle-errors session
      (with-timeout :known-hosts
        (libssh2-knownhost/readfile known-hosts
                                    known-hosts-file
                                    libssh2/KNOWNHOST_FILE_OPENSSH)))))

(defn check
  "Given a session that has already completed a handshake with a remote host,
   check the fingerprint of the remote host against the knonw hosts file."
  [session]
  (let [known-hosts (libssh2-knownhost/init (:session session))
        session-options (:options session)
        file (or (:known-hosts-file session-options)
                 (str (System/getProperty "user.home") "/.ssh/known_hosts"))
        fail-on-mismatch (-> session-options :fail-unless-known-hosts-matches)
        fail-on-missing (-> session-options :fail-if-not-in-known-hosts)]
    (when (nil? known-hosts)
      (throw (Exception. "Failed to initialize known hosts store.")))
    (try
      (load-known-hosts session known-hosts file)
      (check-fingerprint session
                         known-hosts
                         (:host session)
                         (:port session)
                         (host-fingerprint session)
                         fail-on-missing
                         fail-on-mismatch)
      (finally (libssh2-knownhost/free known-hosts)))))
