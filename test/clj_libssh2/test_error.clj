(ns clj-libssh2.test-error
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.session :as libssh2-session]
            [clj-libssh2.error :as error]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(deftest session-error-message-works
  (testing "It's nil safe"
    (is (nil? (error/session-error-message nil))))
  (testing "It returns nil when there was no error"
    (with-redefs [libssh2-session/last-error (constantly 0)]
      (is (nil? (error/session-error-message nil))))))

(deftest maybe-throw-error-works
  (testing "It doesn't throw when the error code is nil"
    (is (nil? (error/maybe-throw-error nil nil))))
  (testing "It doesn't throw unless the error code is negative"
    (is (nil? (error/maybe-throw-error nil 0))))
    (is (nil? (error/maybe-throw-error nil 1))) 
  (testing "It doesn't throw for EAGAIN"
    (is (nil? (error/maybe-throw-error nil libssh2/ERROR_EAGAIN))))
  (testing "It throws on negative error codes"
    (is (thrown? Exception (error/maybe-throw-error nil -1))))
  (testing "It prefers error messages from the session object"
    (let [session-message "This is a fake error message from the session."]
      (with-redefs [error/session-error-message (constantly session-message)]
        (is (thrown-with-msg?
              Exception
              (re-pattern session-message)
              (error/maybe-throw-error nil libssh2/ERROR_ALLOC))))))
  (testing "It produces an error message when there's none from the session."
    (is (thrown-with-msg? Exception
                          #".+"
                          (error/maybe-throw-error nil libssh2/ERROR_ALLOC))))
  (testing "It produces an error message even when the error code is bogus."
    (is (thrown-with-msg? Exception
                          #".+"
                          (error/maybe-throw-error nil -10000)))))
