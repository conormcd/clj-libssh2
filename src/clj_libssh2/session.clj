(ns clj-libssh2.session
  "Functions for creating and managing sessions."
  (:require [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.authentication :refer [authenticate]]
            [clj-libssh2.error :refer [handle-errors with-timeout]]
            [clj-libssh2.known-hosts :as known-hosts]
            [clj-libssh2.socket :as socket]))

(def sessions
  "A pool of currently running sessions. This is an atomic map where the keys
   are session IDs and the values are running session objects."
  (atom {}))

(def default-opts
  "The default options for a session. These are not only the defaults, but an
   exhaustive list of the legal options."
  {:character-set "UTF-8"
   :fail-if-not-in-known-hosts false
   :fail-unless-known-hosts-matches true
   :known-hosts-file nil
   :read-chunk-size (* 1024 1024)
   :read-timeout 60000
   :write-chunk-size (* 1024 1024)})

(defrecord Session [id session socket host port options])

(defn- create-session
  "Make a native libssh2 session object.

   Return:

   A Pointer representing a libssh2 session object. Throws an exception on
   failure."
  []
  (let [session (libssh2-session/init)]
    (when-not session
      (throw (Exception. "Failed to create a libssh2 session.")))
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
  (merge default-opts opts))

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
  ([{session :session} reason raise]
   (handle-errors nil
    (with-timeout :session
      (libssh2-session/disconnect session reason)))
   (handle-errors nil
    (with-timeout :session
      (libssh2-session/free session)))
   (when raise
     (throw (Exception. reason)))))

(defn- handshake
  "Perform the startup handshake with the remote host.

   Arguments:

   session The Session for the connection we're trying to start up.

   Return:

   0 on success. Throws an exception on failure."
  [{session :session socket :socket :as s}]
  (handle-errors s
    (with-timeout :session
      (libssh2-session/handshake session socket))))

(defn- session-id
  "Generate the session ID that will be used to pool sessions.

   Arguments:

   host         The hostname or IP of the remote host.
   port         The port we're connecting to.
   credentials  The credentials to be used to authenticate the session.
   opts         The session options.

   Return:

   A string that will uniquely identify a session."
  [host port credentials opts]
  (format "%s@%s:%d-%d-%d"
          (:username credentials)
          host
          port
          (.hashCode credentials)
          (.hashCode opts)))

(defn close
  "Disconnect an SSH session and discard the session.

   Arguments:

   session The Session that we want to disconnect and close.

   Return:

   nil."
  [{id :id}]
  (when-let [session (get @sessions id)]
    (destroy-session session)
    (socket/close (:socket session))
    (swap! sessions dissoc id)
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
  ([host port credentials]
   (open host port credentials {}))
  ([host port credentials opts]
   (when (empty? @sessions)
     (handle-errors nil (libssh2/init 0)))
   (let [session-options (create-session-options opts)
         id (session-id host port credentials session-options)]
     (if (contains? @sessions id)
       (get @sessions id)
       (let [session (Session. id
                               (create-session)
                               (socket/connect host port)
                               host
                               port
                               session-options)]
         (when (> 0 (:socket session))
           (destroy-session session "Shutting down due to bad socket." true))
         (try
           (libssh2-session/set-blocking (:session session) 0)
           (handshake session)
           (known-hosts/check session)
           (authenticate session credentials)
           (swap! sessions assoc (:id session) session)
           (get @sessions (:id session))
           (catch Exception e
             (close session)
             (throw e))))))))

(defmacro with-session
  "A convenience macro for running some code with a particular session.

   Arguments:

   session        This will be bound to a connected and authenticated Session.
   session-params This is a map where the keys are any valid option (see
                  default-opts) plus :hostname, :port and :credentials which
                  will be passed as the first three arguments to open."
  [session session-params & body]
  `(let [~session (open (:hostname ~session-params)
                        (:port ~session-params)
                        (:credentials ~session-params)
                        (dissoc ~session-params :hostname :port :credentials))]
     (try
       (do ~@body)
       (finally
         (close ~session)))))
