(ns clj-libssh2.session
  "Functions for creating and managing sessions."
  (:require [clojure.set :as set]
            [clojure.tools.logging :as log]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.authentication :refer [authenticate]]
            [clj-libssh2.error :as error :refer [handle-errors]]
            [clj-libssh2.known-hosts :as known-hosts]
            [clj-libssh2.socket :as socket]))

(def sessions
  "An atomic set of currently active sessions. This is used to trigger calls to
   libssh2/init and libssh2/exit at appropriate times. It's also used to
   protect against attempting to close sessions twice."
  (atom #{}))

(def ^:private default-opts
  "The default options for a session. These are not only the defaults, but an
   exhaustive list of the legal options."
  {:character-set "UTF-8"
   :fail-if-not-in-known-hosts false
   :fail-unless-known-hosts-matches true
   :known-hosts-file nil
   :read-chunk-size (* 1024 1024)
   :timeout {:agent 5000        ; All agent operations
             :auth 5000         ; All authentication calls
             :known-hosts 5000  ; All interactions with the known hosts API
             :lib 1000          ; Operations that are 100% local library calls
             :read 60000        ; Reads from the remote host
             :request 5000}     ; Simple calls to the remote host
   :write-chunk-size (* 1024 1024)})

(defrecord Session [session socket host port options])

(defn- create-session
  "Make a native libssh2 session object.

   Return:

   A Pointer representing a libssh2 session object. Throws an exception on
   failure."
  []
  (let [session (libssh2-session/init)]
    (when-not session
      (error/maybe-throw-error nil libssh2/ERROR_ALLOC))
    session))

(defn- create-session-options
  "Take a session options map, do some type/shape enforcement and merge it with
   the defaults.

   Arguments:

   opts A map of session options.

   Return:

   A map of session options which is guaranteed to have a value for all of the
   keys in default-opts."
  [opts]
  {:pre [(map? opts) (every? (set (keys default-opts)) (keys opts))]}
  (merge default-opts
         (assoc opts
                :timeout (merge (:timeout default-opts) (:timeout opts)))))

(defn- destroy-session
  "Free a libssh2 session object from a Session and optionally raise an
   exception.

   Arguments:

   session  The Session that we're shutting down.
   reason   The reason we're disconnecting from the remote host. The default is
            \"Shutting down normally.\"
   raise    Boolean. True if this function should throw an exception after
            shutting down. Defaults to false.

   Return:

   nil or throws an exception if requested."
  ([session]
   (destroy-session session "Shutting down normally." false))
  ([session reason raise]
   (log/info "Tearing down the session.")
   (socket/block session :request
     (handle-errors session
       (libssh2-session/disconnect (:session session) reason)))
   (socket/block session :lib
     (handle-errors session
       (libssh2-session/free (:session session))))
   (when raise
     (error/raise reason {:session session}))))

(defn- handshake
  "Perform the startup handshake with the remote host.

   Arguments:

   session The Session for the connection we're trying to start up.

   Return:

   0 on success. Throws an exception on failure."
  [session]
  (log/info "Handshaking with the remote host.")
  (socket/block session :request
    (handle-errors session
      (libssh2-session/handshake (:session session) (:socket session)))))

(defn close
  "Disconnect an SSH session and discard the session.

   Arguments:

   session The Session that we want to disconnect and close.

   Return:

   nil."
  [session]
  (when (contains? @sessions session)
    (log/info "Closing session.")
    (destroy-session session)
    (socket/close (:socket session))
    (swap! sessions set/difference #{session})
    (when (empty? @sessions)
      (libssh2/exit))))

(defn open
  "Connect to an SSH server, start a session and authenticate it.

   Arguments:

   host         The hostname or IP of the remote host.
   port         The port to connect to.
   credentials  An instance of clj-libssh2.authentication.Credentials or a map
                that can be transformed into one.
   opts         A map with overrides for the default options.

   Return:

   A Session object for the connected and authenticated session."
  [host port credentials opts]
  (log/info "Starting new session.")
  (when (empty? @sessions)
    (handle-errors nil (libssh2/init 0)))
  (let [session (Session. (create-session)
                          (socket/connect host port)
                          host
                          port
                          (create-session-options opts))]
    (when (> 0 (:socket session))
      (destroy-session session "Shutting down due to bad socket." true))
    (try
      (libssh2-session/set-blocking (:session session) 0)
      (handshake session)
      (known-hosts/check session)
      (authenticate session credentials)
      (swap! sessions conj session)
      session
      (catch Throwable t
        (close session)
        (error/raise t)))))

(defmacro with-session
  "A convenience macro for running some code with a particular session.

   Arguments:

   session        This will be bound to a connected and authenticated Session.
   session-params This is a map where the keys are any valid option (see
                  default-opts) plus :hostname, :port and :credentials which
                  will be passed as the first three arguments to open."
  [session session-params & body]
  `(let [session-params# ~session-params
         ~session (open (:hostname session-params#)
                        (:port session-params#)
                        (:credentials session-params#)
                        (dissoc session-params# :hostname :port :credentials))]
     (try
       (do ~@body)
       (finally
         (close ~session)))))
