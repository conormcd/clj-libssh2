(ns clj-libssh2.session
  (:require [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.authentication :refer [authenticate]]
            [clj-libssh2.error :refer [handle-errors]]
            [clj-libssh2.socket :as socket]))

(def sessions (atom {}))

(defn close
  [{id :id session :session socket :socket}]
  (libssh2-session/disconnect session "Shutting down normally")
  (libssh2-session/free session)
  (socket/close socket)
  (when id
    (swap! sessions dissoc id))
  (when (empty? @sessions)
    (libssh2/exit)))

(defn- session-id
  [host port credentials]
  (format "%s@%s:%d" (:username credentials) host port))

(defn- handshake
  [{session :session socket :socket :as s}]
  (handle-errors s (libssh2-session/handshake session socket)))

(defn open
  "Connect to an SSH server and start a session."
  [host port credentials]
  (when (empty? @sessions)
    (handle-errors nil (libssh2/init 0)))
  (let [abort (fn [message]
                (throw (Exception. (format "session/open: %s" message))))
        session {:session (libssh2-session/init)
                 :socket (socket/connect host port)}]
    (when (nil? (:session session))
      (abort "Failed to run libss2_session_init"))
    (when (> 0 (:socket session))
      (abort (format "Failed to connect to %s:%d" host port)))
    (try
      (handshake session)
      (authenticate session credentials)
      (let [id (session-id host port credentials)]
        (swap! sessions assoc id (assoc session :id id))
        (get @sessions id))
      (catch Exception e
        (close session)
        (throw e)))))
