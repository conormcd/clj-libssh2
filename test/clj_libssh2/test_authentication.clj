(ns clj-libssh2.test-authentication
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.userauth :as libssh2-userauth]
            [clj-libssh2.test-utils :as test])
  (:use clj-libssh2.authentication))

(test/fixtures)

; This is more fully tested in clj-libssh2.test-agent
(deftest agent-authentication-works
  (is (test/auth {:credentials (->AgentCredentials (test/ssh-user))})))

(deftest key-authentication-works
  (let [user (test/ssh-user)
        privkey (fn [keyname] (format "test/tmp/id_rsa%s" keyname))
        pubkey (fn [keyname] (str (privkey keyname) ".pub"))
        no-passphrase (->KeyCredentials user "" (privkey "") (pubkey ""))
        with-passphrase (->KeyCredentials user
                                          "correct horse battery staple"
                                          (privkey "_with_passphrase")
                                          (pubkey "_with_passphrase"))
        with-wrong-passphrase (->KeyCredentials user
                                                "bad horse"
                                                (privkey "_with_passphrase")
                                                (pubkey "_with_passphrase"))
        unauthorized (->KeyCredentials user
                                       ""
                                       (privkey "_never_authorised")
                                       (pubkey "_never_authorised"))
        bad-privkey (->KeyCredentials user "" (privkey "") "/bad")
        bad-pubkey (->KeyCredentials user "" "/bad" (pubkey ""))]
    (testing "A passphrase-less key works"
      (is (valid? no-passphrase))
      (is (test/auth {:credentials no-passphrase})))
    (testing "A key with a passphrase works"
      (is (valid? with-passphrase))
      (is (test/auth {:credentials with-passphrase})))
    (testing "A valid but unauthorized key does not work"
      (is (valid? unauthorized))
      (is (thrown? Exception (test/auth {:credentials unauthorized}))))
    (testing "It fails if the private key file doesn't exist"
      (is (valid? bad-privkey))
      (is (thrown? Exception (test/auth {:credentials bad-privkey}))))
    (testing "It fails if the public key file doesn't exist"
      (is (valid? bad-pubkey))
      (is (thrown? Exception (test/auth {:credentials bad-pubkey}))))
    (testing "It fails if the passphrase is incorrect"
      (is (valid? with-wrong-passphrase))
      (is (thrown? Exception (test/auth {:credentials with-wrong-passphrase}))))))

; We can't test this all the way without knowing a password on the local
; machine. We can test with libssh2_userauth_password stubbed and some error
; cases as well. In any case, password authentication is disabled by default on
; most sshd installations (in favour of publickey and keyboard-interactive) so
; we expect this method to only be used in odd circumstances.
(deftest password-authentication-works
  (let [password-creds (fn [password]
                         (->PasswordCredentials (test/ssh-user) password))]
    (testing "A successful authentication returns true"
      (with-redefs [libssh2-userauth/password (constantly 0)]
        (is (test/auth {:credentials (password-creds "doesn't matter")}))))
    (testing "It fails to authenticate with the wrong password"
      (is (thrown? Exception (test/auth {:credentials (password-creds "the wrong password")}))))
    (testing "A library error does not result in a crash"
      (with-redefs [libssh2-userauth/password (constantly libssh2/ERROR_ALLOC)]
        (is (thrown? Exception (test/auth {:credentials (password-creds "doesn't matter")})))))))

(deftest authenticating-with-a-map-fails-if-there's-no-equivalent-record
  (is (thrown-with-msg?
        Exception
        #"Failed to determine credentials type"
        (test/auth {:credentials {:username nil
                                  :password "foo"}}))))
