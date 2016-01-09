(ns clj-libssh2.error
  (:require [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session])
  (:import [com.sun.jna.ptr IntByReference PointerByReference]))

(def timeouts (atom {:agent 10000
                     :auth 10000
                     :known-hosts 10000
                     :session 10000}))

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

(defn session-error-message
  "Call libssh2_session_last_error and return the error message given or nil if
   there was no error."
  [session]
  (when session
    (let [len (IntByReference.)
          buf (PointerByReference.)
          res (libssh2-session/last-error session buf len 0)]
      (when (not= 0 res)
        (String. (.getByteArray (.getValue buf) 0 (.getValue len)) "ASCII")))))

(defn maybe-throw-error
  "If the provided error code is a negative number and is not equal to
   LIBSSH2_ERROR_EAGAIN then an exception will be thrown. An attempt will be
   made to construct a relevant human-readable error message from either the
   session object or from the table of fixed error messages above."
  [session error-code]
  (when (and (some? error-code)
             (> 0 error-code)
             (not= libssh2/ERROR_EAGAIN error-code))
    (let [session-message (session-error-message session)
          default-message (get error-messages error-code)]
      (throw (ex-info (or session-message
                          default-message
                          (format "An unknown error occurred: %d" error-code))
                      {:error default-message
                       :session-error session-message
                       :error-code error-code})))))

(defmacro handle-errors
  "Run some code that might return a negative number to indicate an error.
   Raise an exception if this happens. Pass a non-nil session if you want more
   relevant error messages."
  [session & body]
  `(let [res# (do ~@body)]
     (maybe-throw-error (:session ~session) res#)
     res#))

(defn get-timeout
  "Get a timeout value."
  [name-or-value]
  (or (get @timeouts name-or-value) name-or-value 1000))

(defn set-timeout
  "Update a named timeout value."
  [timeout-name millis]
  (swap! timeouts assoc timeout-name millis))

(defmacro with-timeout
  "Run some code that could return libssh2/ERROR_EAGAIN and if it does, retry
   until the timeout is hit.

   `timeout` can be either a number of milliseconds or a keyword referring to a
   named timeout set using `set-timeout`.

   This will *not* interrupt a blocking function as this is usually used with
   native functions which probably should not be interrupted."
  [timeout & body]
  `(let [start# (System/currentTimeMillis)
         timeout# (get-timeout ~timeout)]
     (loop [timedout# false]
       (if timedout#
         (throw (Exception. "Timeout!"))
         (let [r# (do ~@body)]
           (if (= r# libssh2/ERROR_EAGAIN)
             (recur (< timeout# (- (System/currentTimeMillis) start#)))
             r#))))))
