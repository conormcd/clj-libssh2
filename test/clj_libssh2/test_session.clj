(ns clj-libssh2.test-session
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.session :as session]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(defn- open
  [& creds]
  (session/open test/ssh-host
                test/ssh-port
                (merge {:username (test/ssh-user)}
                       (apply hash-map creds))))

(deftest close-is-robust
  (testing "Closing a good, open connection does not fail."
    (let [session (open)]
      (is (= 1 (count @session/sessions)))
      (session/close session)
      (is (= 0 (count @session/sessions)))))
  (testing "Closing a good, open connection more than once does not fail."
    (let [session (open)]
      (is (= 1 (count @session/sessions)))
      (session/close session)
      (is (= 0 (count @session/sessions)))
      (session/close session)
      (is (= 0 (count @session/sessions)))))
  (testing "Closing nil does not fail."
    (session/close nil)))

(deftest open-works
  (testing "sessions are pooled"
    (is (= 0 (count @session/sessions)))
    (let [session1 (open)]
      (is (= 1 (count @session/sessions)))
      (let [session2 (open)]
        (is (= 1 (count @session/sessions)))
        (is (= session1 session2))
        (session/close session1)
        (session/close session2)
        (is (= 0 (count @session/sessions))))))
  (testing "throws but doesn't crash on handshake failure"
    (with-redefs [libssh2-session/handshake (constantly libssh2/ERROR_PROTO)]
      (is (= 0 (count @session/sessions)))
      (is (thrown? Exception (open)))
      (is (= 0 (count @session/sessions)))))
  (testing "throws but doesn't crash on authentication failure"
      (is (= 0 (count @session/sessions)))
      (is (thrown? Exception (open :password "totally-wrong-password")))
      (is (= 0 (count @session/sessions))))
  (testing "open rejects bad options"
    (is (= 0 (count @session/sessions)))
    (is (thrown? AssertionError (session/open test/ssh-host
                                              test/ssh-port
                                              {:username (test/ssh-user)}
                                              {:bad-option :bad-option-value})))
    (is (= 0 (count @session/sessions)))
    ))
