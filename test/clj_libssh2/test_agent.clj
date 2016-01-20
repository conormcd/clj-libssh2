(ns clj-libssh2.test-agent
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.agent :as libssh2-agent]
            [clj-libssh2.session :as session]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(defn agent-session
  []
  (session/open test/ssh-host test/ssh-port {:username (test/ssh-user)} {}))

(defn open-and-close
  []
  (is (= 0 (count @session/sessions)))
  (let [session (agent-session)]
    (is (= 1 (count @session/sessions)))
    (session/close session)
    (is (= 0 (count @session/sessions)))))

(deftest agent-authentication-works
  (testing "A good session works"
    (open-and-close))
  (testing "If no identities match, we get an exception"
    (with-redefs [libssh2-agent/userauth (constantly libssh2/ERROR_PUBLICKEY_UNVERIFIED)]
      (is (thrown? Exception (open-and-close)))))
  (testing "If there are no identities, we get an exception"
    (with-redefs [libssh2-agent/get-identity (constantly 1)]
      (is (thrown? Exception (open-and-close))))))

(deftest agent-authentication-throws-but-doesn't-crash
  (testing "when libssh2_agent_init fails"
    (with-redefs [libssh2-agent/init (constantly nil)]
      (is (thrown? Exception (open-and-close)))))
  (testing "when libssh2_agent_connect fails"
    (with-redefs [libssh2-agent/connect (constantly libssh2/ERROR_AGENT_PROTOCOL)]
      (is (thrown? Exception (open-and-close)))))
  (testing "when libssh2_agent_list_identities fails"
    (with-redefs [libssh2-agent/list-identities (constantly libssh2/ERROR_BAD_USE)]
      (is (thrown? Exception (open-and-close)))))
  (testing "when libssh2_agent_get_identity fails"
    (with-redefs [libssh2-agent/get-identity (constantly libssh2/ERROR_AGENT_PROTOCOL)]
      (is (thrown? Exception (open-and-close)))))
  (testing "when libssh2_agent_disconnect fails"
    (with-redefs [libssh2-agent/disconnect (constantly libssh2/ERROR_SOCKET_DISCONNECT)]
      (is (thrown? Exception (open-and-close))))))
