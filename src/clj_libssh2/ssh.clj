(ns clj-libssh2.ssh
  (:require [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]
            [clj-libssh2.channel :as channel]
            [clj-libssh2.error :as error]
            [clj-libssh2.libssh2.scp :as libssh2-scp]
            [clj-libssh2.session :as session]
            [clj-libssh2.socket :as socket])
  (:import [java.io ByteArrayInputStream
                    ByteArrayOutputStream
                    FileInputStream
                    FileOutputStream
                    InputStream
                    OutputStream]
           [clj_libssh2.session Session]))

(defmacro with-session
  "Convenience macro for dealing with sessions.

   Arguments:

   session          This will be bound to a valid clj-libssh2.session.Session.
   session-or-host  This is either a clj-libssh2.session.Session object (in
                    which case this macro simply binds it to session) or a map
                    describing a potential session (in which case this calls
                    clj-libssh2.session/with-session to create the session)."
  [session session-or-host & body]
  `(if (instance? Session ~session-or-host)
     (let [~session ~session-or-host]
       (do ~@body))
     (let [defaults# {:hostname "127.0.0.1"
                      :port 22
                      :credentials {:username (System/getProperty "user.name")}}]
       (session/with-session ~session (merge defaults# ~session-or-host)
         (do ~@body)))))

(defn exec
  "Execute a command and get the results.

   If you're executing a command and don't care about the output you should
   redirect its output on the remote host to /dev/null. This function will
   always read the output across the wire even if it won't be returned as this
   is the only reliable way to wait for the exit status to be correctly set.

   Arguments:

   session-or-host  Either a valid clj-libssh2.session.Session object or a map
                    suitable for handing off to with-session.
   commandline      The command which should be executed.

   Optional keyword arguments:

   :in  A String or an InputStream which will be fed to the standard input of
        the remote process. If you provide an InputStream which is an instance
        of PushbackInputStream you must ensure that the size of the
        PushbackInputStream equals or exceeds the :write-chunk-size for the
        session.
   :out An OutputStream. The standard output of the remote process will be
        written to this stream. If this is nil, then output will be discarded.
        If this is not provided, then the output will be returned as a String.
   :err An OutputStream which will be connected to the standard error of the
        remote process and in every other way behaves like :out.
   :env A map of environment variables which will be set before the command is
        executed. The keys are the environment variable names. The values are
        the values for those variables. The setting of environment variables is
        controlled by the remote sshd and the value of its AcceptEnv
        configuration variable.

   Return:

   A map with the following keys and values.

   :out     The output from standard output on the remote host. If you specify
            a (potentially nil) output stream in the arguments to this function
            then that output stream will be returned. If no output stream was
            provided then this value will be a String containing the output.
   :err     The contents of standard error from the remote host, in the same
            way as :out was provided.
   :exit    The exit code from the remote process.
   :signal  A map with the keys :exit-signal :err-msg and :lang-tag which have
            the values from the similarly-named out arguments of
            libssh2_channel_get_exit_signal."
  [session-or-host commandline & {:as io}]
  (log/info "Begin ssh/exec")
  (with-session session session-or-host
    (let [charset (-> session :options :character-set)
          stdin (if (contains? io :in)
                  (if (instance? String (:in io))
                    (-> io :in (.getBytes charset) (ByteArrayInputStream.))
                    (:in io))
                  nil)
          stdout (if (contains? io :out) (:out io) (ByteArrayOutputStream.))
          stderr (if (contains? io :err) (:err io) (ByteArrayOutputStream.))
          inputs (if stdin {0 stdin} {})
          outputs {0 stdout 1 stderr}]
      (channel/with-channel session channel
        (channel/setenv session channel (:env io))
        (channel/exec session channel commandline)
        (let [streams (channel/pump session channel inputs outputs)
              out (:stream (get streams 0))
              err (:stream (get streams 1))]
          (channel/close session channel)
          {:out (if (contains? io :out) out (.toString out charset))
           :err (if (contains? io :err) err (.toString err charset))
           :exit (channel/exit-status channel)
           :signal (channel/exit-signal session channel)})))))

(defn scp-from
  "Retrieve a file from a remote machine using SCP.

   Arguments:

   session-or-host  Either a valid clj-libssh2.session.Session object or a map
                    suitable for handing off to with-session.
   remote-path      The path on the remote machine of the file you wish to
                    retrieve.
   local-path       The location on the local machine where the file should be
                    copied to.

   Return:

   A map with the following keys and values:

   :local-path  The local-path argument.
   :remote-path The remote-path argument.
   :size        The size of the file as read.
   :remote-stat A map with the following keys and values, corresponding to the
                fields in a struct stat as reported by the remote host:

                :atime  The last access time of the remote file.
                :ctime  The last time the remote file's metadata changed.
                :gid    The group ID of the remote file.
                :mode   The permission mask for the remote file.
                :mtime  The last modified time for the remote file.
                :size   The size of the file on the remote system.
                :uid    The user ID of the remote file.

   Note 1:  The destination file at local-path _may_ exist even after this
            function throws an exception. It's up to the caller to handle
            partial downloads.
   Note 2:  Given a return map `m` (-> m :size) and (-> m :remote-stat :size)
            should be equal. If they are not equal then steps should be taken
            to verify the download."
  [session-or-host remote-path local-path]
  (log/info "Begin ssh/scp-from")
  (with-session session session-or-host
    (channel/with-scp-recv-channel session channel remote-path fileinfo
      (let [output (FileOutputStream. local-path)
            file-size (.getSize fileinfo)
            read-chunk-size (-> session :options :read-chunk-size)
            read-timeout (-> session :options :read-timeout)
            finish (fn [bytes-read]
                     (.close output)
                     {:local-path local-path
                      :remote-path remote-path
                      :size bytes-read
                      :remote-stat {:atime (.getATime fileinfo)
                                    :ctime (.getCTime fileinfo)
                                    :gid (.getGroupID fileinfo)
                                    :mode (.getMode fileinfo)
                                    :mtime (.getMTime fileinfo)
                                    :size file-size
                                    :uid (.getUserID fileinfo)}})]
        (loop [bytes-read 0
               last-read (System/currentTimeMillis)]
          (when (< read-timeout (- (System/currentTimeMillis) last-read))
            (error/raise "Read timeout while receiving file"
                         {:remote-path remote-path
                          :local-path local-path
                          :bytes-read bytes-read
                          :timeout read-timeout
                          :session session}))
          (if (< bytes-read file-size)
            (let [read-size (min (- file-size bytes-read) read-chunk-size)
                  res (channel/read session channel 0 read-size)
                  status (:status res)
                  received (:received res)
                  data (:data res)]
              (if (not= :eof status)
                (do
                  (when (< 0 received)
                    (.write output data 0 received))
                  (when (= :eagain status)
                    (socket/wait))
                  (recur (+ bytes-read received) (System/currentTimeMillis)))
                (finish (+ bytes-read received))))
            (finish bytes-read)))))))

(defn scp-to
  "Send a file to a remote machine using SCP.

   Arguments:

   session-or-host  Either a valid clj-libssh2.session.Session object or a map
                    suitable for handing off to with-session.
   local-path       The location on the local machine where the file should be
                    copied from.
   remote-path      The path on the remote machine where the file should be
                    placed.

   Optional keyword arguments:

   :atime The last access time which the remote file should have.
   :mode  The permissions mask which should be applied to the remote file.
   :mtime The last modified time which the remote file should have.

   Returns:

   nil"
  [session-or-host local-path remote-path & {:keys [atime mode mtime]
                                             :as props}]
  (log/info "Begin ssh/scp-to")
  (with-session session session-or-host
    (let [local-file (file local-path)
          input (FileInputStream. local-file)
          props (merge {:size (.length local-file)
                        :mtime (.lastModified local-file)} props)]
      (channel/with-scp-send-channel session channel remote-path props
        (channel/pump session channel {0 input} {})))))
