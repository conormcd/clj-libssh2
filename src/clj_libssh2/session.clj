(ns clj-libssh2.session
  (:require [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.authentication :refer [authenticate]]
            [clj-libssh2.error :refer [handle-errors]]
            [clj-libssh2.socket :as socket]))

(def sessions (atom {}))

(defrecord Session [id session socket])

(defn- create-session
  "Make a native libssh2 session object."
  []
  (let [session (libssh2-session/init)]
    (when-not session
      (throw (Exception. "Failed to create a libssh2 session.")))
    session))

(defn- destroy-session
  "Free a libssh2 session object from a Session."
  ([^Session session]
   (destroy-session session "Shutting down normally." false))
  ([^Session {session :session} ^String reason ^Boolean raise]
   (handle-errors nil (libssh2-session/disconnect session reason))
   (handle-errors nil (libssh2-session/free session))
   (when raise
     (throw (Exception. reason)))))

(defn- handshake
  [^Session {session :session socket :socket :as s}]
  (handle-errors s (libssh2-session/handshake session socket)))

(defn- session-id
  "Generate the session ID that will be used to pool"
  [host port credentials]
  (format "%s@%s:%d" (:username credentials) host port))

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
  [host port credentials]
  (when (empty? @sessions)
    (handle-errors nil (libssh2/init 0)))
  (let [id (session-id host port credentials)]
    (if (contains? @sessions id)
      (get @sessions id)
      (let [session (Session. (session-id host port credentials)
                              (create-session)
                              (socket/connect host port))]
        (when (> 0 (:socket session))
          (destroy-session session "Shutting down due to bad socket." true))
        (try
          (handshake session)
          (authenticate session credentials)
          (swap! sessions assoc (:id session) session)
          (get @sessions (:id session))
          (catch Exception e
            (close session)
            (throw e)))))))
