(ns clj-libssh2.agent
  (:require [clj-libssh2.error :refer [handle-errors]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.agent :as libssh2-agent])
  (:import [com.sun.jna.ptr PointerByReference]))

(defn- get-identity
  "Get the next available identity from the agent. Pass nil for previous to get
   the first entry."
  [session ssh-agent previous]
  (when (nil? previous)
    (handle-errors session (libssh2-agent/list-identities ssh-agent)))
  (let [id (PointerByReference.)
        ret (handle-errors session
              (libssh2-agent/get-identity ssh-agent id previous))]
    (case ret
      0 (.getValue id)
      1 nil
      (throw (Exception. "An unknown error occurred")))))

(defn authenticate
  "Attempt to authenticate a session using the agent."
  [session username]
  (let [ssh-agent (libssh2-agent/init (:session session))]
    (when (nil? ssh-agent)
      (throw (Exception. "Failed to initialize agent.")))
    (try
      (handle-errors session (libssh2-agent/connect ssh-agent))
      (when-not (loop [success false
                       previous nil]
                  (if success
                    success
                    (if-let [id (get-identity session ssh-agent previous)]
                      (recur
                        (= 0 (libssh2-agent/userauth ssh-agent username id))
                        id)
                      false)))
        (throw (Exception. "Failed to authenticate with the agent.")))
      true
      (finally
        (handle-errors session (libssh2-agent/disconnect ssh-agent))
        (libssh2-agent/free ssh-agent)))))
