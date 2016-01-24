(ns clj-libssh2.agent
  "Functions for interacting with an SSH agent. The agent is expected to be
   available on the UNIX domain socket referred to by the SSH_AUTH_SOCK
   environment variable."
  (:require [clj-libssh2.error :as error :refer [handle-errors with-timeout]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.agent :as libssh2-agent])
  (:import [com.sun.jna.ptr PointerByReference]))

(defn- get-identity
  "Get the next available identity from the agent. Pass nil for previous to get
   the first entry.

   Arguments:

   session    A clj-libssh2.session.Session object.
   ssh-agent  An ssh agent object from libssh2_agent_init.
   previous   The last identity returned from a call to this function. Pass nil
              to get the first entry.

   Return:

   A native object as returned by libssh2_agent_get_identity."
  [session ssh-agent previous]
  (when (nil? previous)
    (handle-errors session
      (with-timeout :agent
        (libssh2-agent/list-identities ssh-agent))))
  (let [id (PointerByReference.)
        ret (handle-errors session
              (with-timeout :agent
                (libssh2-agent/get-identity ssh-agent id previous)))]
    (case ret
      0 (.getValue id)
      1 nil
      (throw (ex-info "libssh2_agent_get_identity returned a bad value."
                      {:function "libssh2_agent_get_identity"
                       :return ret
                       :session session})))))

(defn authenticate
  "Attempt to authenticate a session using the agent.

   Arguments:

   session  A clj-libssh2.session.Session object which refers to a session
            which has not already been authenticated.
   username The username for the user who is trying to authenticate.

   Return:

   True on success. An exception will be thrown if the user could not be
   authenticated."
  [session username]
  (let [ssh-agent (libssh2-agent/init (:session session))]
    (when (nil? ssh-agent)
      (error/maybe-throw-error session libssh2/ERROR_ALLOC))
    (try
      (handle-errors session
        (with-timeout :agent
          (libssh2-agent/connect ssh-agent)))
      (when-not (loop [success false
                       previous nil]
                  (if success
                    success
                    (if-let [id (get-identity session ssh-agent previous)]
                      (recur
                        (= 0 (with-timeout :agent
                               (libssh2-agent/userauth ssh-agent username id)))
                        id)
                      false)))
        (throw (ex-info "Failed to authenticate using the SSH agent."
                        {:username username
                         :session session})))
      true
      (finally
        (handle-errors session
          (with-timeout :agent
            (libssh2-agent/disconnect ssh-agent)))
        (libssh2-agent/free ssh-agent)))))
