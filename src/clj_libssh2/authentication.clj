(ns clj-libssh2.authentication
  (:require [clojure.java.io :refer [file]]
            [clj-libssh2.libssh2.userauth :as libssh2-userauth]
            [clj-libssh2.agent :as ssh-agent]
            [clj-libssh2.error :refer [handle-errors]])
  (:import clojure.lang.PersistentArrayMap))

(defprotocol Credentials
  (valid? [credentials]))

(defrecord AgentCredentials [username]
  Credentials
  (valid? [credentials] (some? username)))

(defrecord KeyCredentials [username passphrase private-key public-key]
  Credentials
  (valid? [credentials] (and (some? username)
                             (some? passphrase)
                             (some? private-key)
                             (some? public-key)
                             (.exists (file private-key))
                             (.exists (file public-key)))))

(defrecord PasswordCredentials [username password]
  Credentials
  (valid? [credentials] (and (some? username) (some? password))))

(defmulti authenticate
  (fn [session credentials] (type credentials)))

(defmethod authenticate AgentCredentials
  [session credentials]
  (ssh-agent/authenticate session (:username credentials)))

(defmethod authenticate KeyCredentials
  [session credentials]
  (doseq [keyfile (map #(% credentials) [:private-key :public-key])]
    (when-not (.exists (file keyfile))
      (throw (Exception. (format "%s does not exist." keyfile)))))
  (handle-errors session
    (libssh2-userauth/publickey-fromfile (:session session)
                                         (:username credentials)
                                         (:public-key credentials)
                                         (:private-key credentials)
                                         (:passphrase credentials)))
  true)

(defmethod authenticate PasswordCredentials
  [session credentials]
  (handle-errors session
    (libssh2-userauth/password (:session session)
                               (:username credentials)
                               (:password credentials)))
  true)

(defmethod authenticate PersistentArrayMap
  [session credentials]
  (loop [m [map->KeyCredentials map->PasswordCredentials map->AgentCredentials]]
    (let [creds ((first m) credentials)]
      (if (valid? creds)
        (authenticate session creds)
        (if (< 1 (count m))
          (recur (rest m))
          (throw (Exception. "Invalid credentials")))))))
