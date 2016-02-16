(ns clj-libssh2.authentication
  "Authenticate a session."
  (:require [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]
            [clj-libssh2.agent :as agent]
            [clj-libssh2.error :as error :refer [handle-errors with-timeout]]
            [clj-libssh2.libssh2.userauth :as libssh2-userauth])
  (:import [java.io FileNotFoundException]
           [clojure.lang PersistentArrayMap]))

(defprotocol Credentials
  "A datatype to represent a way of authentication and the necessary data to
   use that authentication method."
  (valid? [credentials]
    "Check if this Credentials instance is internally consistent."))

(defrecord AgentCredentials [username]
  Credentials
  (valid? [credentials] (some? username)))

(defrecord KeyCredentials [username passphrase private-key public-key]
  Credentials
  (valid? [credentials] (and (some? username)
                             (some? passphrase)
                             (some? private-key)
                             (some? public-key))))

(defrecord PasswordCredentials [username password]
  Credentials
  (valid? [credentials] (and (some? username) (some? password))))

(defmulti authenticate
  "Authenticate a session.

   Arguments:

   session      A clj-libssh2.session.Session object referring to an SSH
                session which has not yet been authenticated.
   credentials  Either an instance of Credentials or a map which can be
                transformed into a Credentials object.

   Return:

   True on success. An exception will be thrown if ther session could not be
   authenticated."
  {:arglists '([session credentials])}
  (fn [session credentials] (type credentials)))

(defmethod authenticate AgentCredentials
  [session credentials]
  (log/info "Authenticating using an SSH agent.")
  (agent/authenticate session (:username credentials)))

(defmethod authenticate KeyCredentials
  [session credentials]
  (log/info "Authenticating using a keypair.")
  (doseq [keyfile (map #(% credentials) [:private-key :public-key])]
    (when-not (.exists (file keyfile))
      (error/raise (FileNotFoundException. keyfile))))
  (handle-errors session
    (with-timeout session :auth
      (libssh2-userauth/publickey-fromfile (:session session)
                                           (:username credentials)
                                           (:public-key credentials)
                                           (:private-key credentials)
                                           (:passphrase credentials))))
  true)

(defmethod authenticate PasswordCredentials
  [session credentials]
  (log/info "Authenticating using a username and password.")
  (handle-errors session
    (with-timeout session :auth
      (libssh2-userauth/password (:session session)
                                 (:username credentials)
                                 (:password credentials))))
  true)

(defmethod authenticate PersistentArrayMap
  [session credentials]
  (log/info "Deriving correct credential type from a map...")
  (loop [m [map->KeyCredentials map->PasswordCredentials map->AgentCredentials]]
    (let [creds ((first m) credentials)]
      (if (valid? creds)
        (authenticate session creds)
        (if (< 1 (count m))
          (recur (rest m))
          (error/raise "Failed to determine credentials type."
                       {:items (keys credentials)
                        :session session}))))))
