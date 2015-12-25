(ns clj-libssh2.authentication
  (:require [clojure.java.io :refer [file]]
            [clj-libssh2.libssh2.userauth :as libssh2-userauth]
            [clj-libssh2.agent :as ssh-agent]
            [clj-libssh2.error :refer [handle-errors]]))

(defmulti authenticate
  (fn [session credentials]
    (cond
      (and (:username credentials) (:agent credentials))
      :agent

      (and (:passphrase credentials)
           (:public-key credentials)
           (:private-key credentials)
           (:username credentials))
      :key

      (and (:username credentials) (:password credentials))
      :password

      :else :invalid)))

(defmethod authenticate :invalid
  [session credentials]
  (throw (Exception. "Invalid credentials.")))

(defmethod authenticate :agent
  [session credentials]
  (ssh-agent/authenticate session (:username credentials)))

(defmethod authenticate :key
  [session credentials]
  (let [require-exists (fn [path]
                         (when-not (.exists (file path))
                           (throw (Exception.
                                    (format "%s does not exist." path)))))
        passphrase (:passphrase credentials)
        privkey (:private-key credentials)
        pubkey (:public-key credentials)
        username (:username credentials)]
    (require-exists privkey)
    (require-exists pubkey)
    (handle-errors session
      (libssh2-userauth/publickey-fromfile (:session session)
                                           username
                                           pubkey
                                           privkey
                                           passphrase))))

(defmethod authenticate :password
  [session credentials]
  (let [username (:username credentials)
        password (:password credentials)]
    (handle-errors session
      (libssh2-userauth/password (:session session) username password))))
