(ns clj-libssh2.session
  (:require [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.authentication :refer [authenticate]]
            [clj-libssh2.error :refer [handle-errors with-timeout]]
            [clj-libssh2.known-hosts :as known-hosts]
            [clj-libssh2.socket :as socket]))

(def sessions (atom {}))

; The default options for a session. These are not only the defaults, but an
; exhaustive list of the legal options.
(def default-opts
  {:fail-if-not-in-known-hosts false
   :fail-unless-known-hosts-matches true
   :known-hosts-file nil})

(defrecord Session [id session socket host port options])

(defn- create-session
  "Make a native libssh2 session object."
  []
  (let [session (libssh2-session/init)]
    (when-not session
      (throw (Exception. "Failed to create a libssh2 session.")))
    session))

(defn- create-session-options
  [opts]
  {:pre [(every? (set (keys default-opts)) (keys opts))]}
  (merge default-opts opts))

(defn- destroy-session
  "Free a libssh2 session object from a Session."
  ([^Session session]
   (destroy-session session "Shutting down normally." false))
  ([^Session {session :session} ^String reason ^Boolean raise]
   (handle-errors nil
    (with-timeout :session
      (libssh2-session/disconnect session reason)))
   (handle-errors nil
    (with-timeout :session
      (libssh2-session/free session)))
   (when raise
     (throw (Exception. reason)))))

(defn- handshake
  [^Session {session :session socket :socket :as s}]
  (handle-errors s
    (with-timeout :session
      (libssh2-session/handshake session socket))))

(defn- session-id
  "Generate the session ID that will be used to pool sessions."
  [host port credentials opts]
  (format "%s@%s:%d-%d-%d"
          (:username credentials)
          host
          port
          (.hashCode credentials)
          (.hashCode opts)))

(defn close
  "Disconnect an SSH session and discard the session."
  [^Session {id :id}]
  (when-let [session (get @sessions id)]
    (destroy-session session)
    (socket/close (:socket session))
    (swap! sessions dissoc id)
    (when (empty? @sessions)
      (libssh2/exit))))

(defn open
  "Connect to an SSH server and start a session."
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
                               (create-session-options opts))]
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

(defn get-timeout
  [session]
  (libssh2-session/get-timeout (:session session)))
