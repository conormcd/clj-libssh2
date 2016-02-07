(ns clj-libssh2.error
  "Utility functions for making error handling easier when calling native
   functions."
  (:require [clojure.tools.logging :as log]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session])
  (:import [com.sun.jna.ptr IntByReference PointerByReference]))

(def timeouts
  "An atom map where the keys are keywords being the symbolic names of named
   timeouts (see get-timeout/set-timeout) and the values are the number of
   milliseconds for that timeout."
  (atom {:agent 10000
         :auth 10000
         :known-hosts 10000}))

(def error-messages
  "All of the error codes that are documented for libssh2 except for
   LIBSSH2_ERROR_SOCKET_NONE which despite its name is a generic error and
   LIBSSH2_ERROR_EAGAIN which is almost never actually an error.

   These are fallback error messages for when a more appropriate one is not
   available from libssh2_session_last_error()."
  {libssh2/ERROR_BANNER_RECV "Failed to receive banner from remote host."
   libssh2/ERROR_BANNER_SEND "Unable to send banner to remote host."
   libssh2/ERROR_INVALID_MAC "Invalid MAC received."
   libssh2/ERROR_KEX_FAILURE "Encryption key exchange with the remote host failed."
   libssh2/ERROR_ALLOC "An internal memory allocation call failed."
   libssh2/ERROR_SOCKET_SEND "Unable to send data on socket."
   libssh2/ERROR_KEY_EXCHANGE_FAILURE "Unrecoverable error exchanging keys."
   libssh2/ERROR_TIMEOUT "Timed out."
   libssh2/ERROR_HOSTKEY_INIT "Unable to initialize hostkey importer."
   libssh2/ERROR_HOSTKEY_SIGN "Unable to verify hostkey signature."
   libssh2/ERROR_DECRYPT "Failed to decrypt."
   libssh2/ERROR_SOCKET_DISCONNECT "The socket was disconnected."
   libssh2/ERROR_PROTO "An invalid SSH protocol response was received on the socket."
   libssh2/ERROR_PASSWORD_EXPIRED "Password expired."
   libssh2/ERROR_FILE "File I/O error."
   libssh2/ERROR_METHOD_NONE "No method has been set."
   libssh2/ERROR_AUTHENTICATION_FAILED "Authentication failed."
   libssh2/ERROR_PUBLICKEY_UNVERIFIED "The username/public key combination was invalid."
   libssh2/ERROR_CHANNEL_OUTOFORDER "Out of order."
   libssh2/ERROR_CHANNEL_FAILURE "Unknown channel failure."
   libssh2/ERROR_CHANNEL_REQUEST_DENIED "Request denied for channel."
   libssh2/ERROR_CHANNEL_UNKNOWN "Channel not found."
   libssh2/ERROR_CHANNEL_WINDOW_EXCEEDED "Window size exceeded."
   libssh2/ERROR_CHANNEL_PACKET_EXCEEDED "Packet size exceeded."
   libssh2/ERROR_CHANNEL_CLOSED "The channel has been closed."
   libssh2/ERROR_CHANNEL_EOF_SENT "The channel has been requested to be closed."
   libssh2/ERROR_SCP_PROTOCOL "SCP protocol error."
   libssh2/ERROR_ZLIB "Zlib compression/decompression failure."
   libssh2/ERROR_SOCKET_TIMEOUT "Socket timeout."
   libssh2/ERROR_SFTP_PROTOCOL "An invalid SFTP protocol response was received on the socket, or an SFTP operation caused an errorcode to be returned by the server."
   libssh2/ERROR_REQUEST_DENIED "The remote server refused the request."
   libssh2/ERROR_METHOD_NOT_SUPPORTED "The requested method is not supported."
   libssh2/ERROR_INVAL "Invalid parameter for internal function."
   libssh2/ERROR_INVALID_POLL_TYPE "Invalid descriptor passed to libssh2_poll()"
   libssh2/ERROR_PUBLICKEY_PROTOCOL "Unexpected publickey subsystem response."
   libssh2/ERROR_BUFFER_TOO_SMALL "Known-host write buffer too small."
   libssh2/ERROR_BAD_USE "Internal error."
   libssh2/ERROR_COMPRESS "Compression/decompression failure."
   libssh2/ERROR_OUT_OF_BOUNDARY "Packet maximum payload exceeded."
   libssh2/ERROR_AGENT_PROTOCOL "Agent protocol error."
   libssh2/ERROR_SOCKET_RECV "Socket receive error."
   libssh2/ERROR_ENCRYPT "Encryption failure."
   libssh2/ERROR_BAD_SOCKET "Bad socket."
   libssh2/ERROR_KNOWN_HOSTS "Failed to parse known hosts file."})

(defmacro raise
  "Log an error and then throw an exception with the same error message and
   optionally some additional information.

   Arguments:

   message          The message to be logged. This will also be the primary
                    message of the exception. If this message is a Throwable,
                    then the additional information will be discarded and the
                    passed-in Throwable will be rethrown after it's logged.
   additional-info  A map of additional information which might be useful to
                    anyone debugging an error reported by this exception."
  ([message]
   `(raise ~message {}))
  ([message additional-info]
   `(let [message# ~message
          additional-info# ~additional-info]
      (try
        (throw (if (instance? Throwable message#)
                 message#
                 (ex-info message# additional-info#)))
        (catch Exception e#
          (log/log :error e# (.getMessage e#))
          (throw e#))))))

(defn session-error-message
  "Call libssh2_session_last_error and return the error message given or nil if
   there was no error.

   Arguments:

   session The clj-libssh2.session.Session object for the session where the
           error occurred.

   Return:

   The message from libssh2_session_last_error or nil if an error message is
   available and the provided session was not nil."
  [session]
  (when session
    (let [len (IntByReference.)
          buf (PointerByReference.)
          res (libssh2-session/last-error session buf len 0)]
      (when (not= 0 res)
        (String. (.getByteArray (.getValue buf) 0 (.getValue len)) "ASCII")))))

(defn maybe-throw-error
  "Convert an error return from a native function call into an exception, if
   it's warranted.

   Arguments:

   session    The clj-libssh2.session.Session object for the session where the
              error may have occurred.
   error-code An integer return code from a native function.

   Return:

   Nil if the return code was positive or if the return code was equal to
   LIBSSH2_ERROR_EAGAIN. In all other cases an exception is thrown using
   ex-info. The message of the exception is the best available message,
   preferring the message from session-error-message, falling back to the
   matching message in error-messages and finally falling back to a generic
   message containing the numeric value of the error. The additional
   information map attached using ex-info has the following keys and values
   which may be useful to debug the error handling itself:

   :error         The error message from error-messages, if any.
   :error-code    The numeric return value.
   :session       The clj-libssh2.session.Session object, if any.
   :session-error The error message from session-error-message, if any."
  [session error-code]
  (when (and (some? error-code)
             (> 0 error-code)
             (not= libssh2/ERROR_EAGAIN error-code))
    (let [session-message (session-error-message session)
          default-message (get error-messages error-code)]
      (raise (or session-message
                 default-message
                 (format "An unknown error occurred: %d" error-code))
             {:error default-message
              :error-code error-code
              :session session
              :session-error session-message}))))

(defmacro handle-errors
  "Run some code that might return a negative number to indicate an error.
   Raise an exception if this happens.

   Arguments:

   session A clj-libssh2.session.Session which can be used to generate better
           error messages via session-error-message. This may be nil, in which
           case more generic error messages will be used.

   Return:

   The return value of the body, *except* in cases where it's a negative number
   (but not equal to LIBSSH2_ERROR_EAGAIN). In those cases an exception will be
   thrown using maybe-throw-error."
  [session & body]
  `(let [session# ~session
         res# (do ~@body)]
     (maybe-throw-error (:session session#) res#)
     res#))

(defn get-timeout
  "Get a timeout value.

   Arguments:

   name-or-value Either the name of a symbolic timeout (e.g. :session) or a
                 number of milliseconds.

   Return:

   A number of milliseconds for the timeout."
  [name-or-value]
  (or (get @timeouts name-or-value) name-or-value 1000))

(defn set-timeout
  "Update a timeout value.

   Arguments:

   timeout-name The name of a symbolic timeout.
   millis       The number of milliseconds for that timeout.

   Return:

   A map of all the current symbolic timeouts."
  [timeout-name millis]
  (swap! timeouts assoc timeout-name millis))

(defmacro with-timeout
  "Run some code that could return libssh2/ERROR_EAGAIN and if it does, retry
   until the timeout is hit. This will *not* interrupt a blocking function as
   this is usually used with native functions which probably should not be
   interrupted.

   N.B. If the function you're calling will block in order to wait on activity
        on the current session's socket, then you should use
        clj-libssh2.socket/block instead as this will select on the socket and
        produce much better retry behaviour.

   Arguments:

   timeout Either a number of milliseconds or a keyword referring to a named
           timeout set using set-timeout.

   Return:

   Either the return value of the code being wrapped or an exception if the
   timeout is exceeded."
  [timeout & body]
  `(let [start# (System/currentTimeMillis)
         timeout# (get-timeout ~timeout)]
     (loop [timedout# false]
       (if timedout#
         (raise (format "Timeout of %d ms exceeded" timeout#)
                {:timeout ~timeout
                 :timeout-length timeout#})
         (let [r# (do ~@body)]
           (if (= r# libssh2/ERROR_EAGAIN)
             (recur (< timeout# (- (System/currentTimeMillis) start#)))
             r#))))))
