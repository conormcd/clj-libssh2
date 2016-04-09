(ns clj-libssh2.test-agent
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.agent :as libssh2-agent]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(deftest agent-authentication-works
  (testing "A good session works"
    (test/auth))
  (testing "If no identities match, we get an exception"
    (with-redefs [libssh2-agent/userauth (constantly libssh2/ERROR_PUBLICKEY_UNVERIFIED)]
      (is (thrown? Exception (test/auth)))))
  (testing "If there are no identities, we get an exception"
    (with-redefs [libssh2-agent/get-identity (constantly 1)]
      (is (thrown? Exception (test/auth))))))

(deftest agent-authentication-throws-but-doesn't-crash
  (testing "when libssh2_agent_init fails"
    (with-redefs [libssh2-agent/init (constantly nil)]
      (is (thrown? Exception (test/auth)))))
  (testing "when libssh2_agent_connect fails"
    (with-redefs [libssh2-agent/connect (constantly libssh2/ERROR_AGENT_PROTOCOL)]
      (is (thrown? Exception (test/auth)))))
  (testing "when libssh2_agent_list_identities fails"
    (with-redefs [libssh2-agent/list-identities (constantly libssh2/ERROR_BAD_USE)]
      (is (thrown? Exception (test/auth)))))
  (testing "when libssh2_agent_get_identity fails"
    (with-redefs [libssh2-agent/get-identity (constantly libssh2/ERROR_AGENT_PROTOCOL)]
      (is (thrown? Exception (test/auth)))))
  (testing "when libssh2_agent_disconnect fails"
    (with-redefs [libssh2-agent/disconnect (constantly libssh2/ERROR_SOCKET_DISCONNECT)]
      (is (thrown? Exception (test/auth))))))
