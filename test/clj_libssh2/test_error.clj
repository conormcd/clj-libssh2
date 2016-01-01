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

(deftest with-timeout-works
  (let [test-func (fn [& args]
                    (let [state (atom (vec args))]
                      (fn []
                        (when (empty? @state)
                          (throw (Exception. "Called too many times!")))
                        (let [result (first @state)
                              new-state (rest @state)]
                          (reset! state new-state)
                          (if (instance? clojure.lang.Fn result)
                            (result)
                            result)))))]
    (testing "test-func behaves as expected"
      (let [f (test-func 1 (constantly 2) 3)]
        (is (= 1 (f)))
        (is (= 2 (f)))
        (is (= 3 (f)))
        (is (thrown? Exception (f)))))
    (testing "with-timeout doesn't retry successful function calls"
      (let [f (test-func 0)]
        (is (= 0 (error/with-timeout 10000 (f))))))
    (testing "with-timeout doesn't retry failed function calls"
      (let [f (test-func libssh2/ERROR_ALLOC)]
        (is (= libssh2/ERROR_ALLOC (error/with-timeout 10000 (f))))))
    (testing "with-timeout retries when it sees EAGAIN"
      (let [f (test-func libssh2/ERROR_EAGAIN 0)]
        (is (= 0 (error/with-timeout 10000 (f))))))
    (testing "with-timeout doesn't retry exceptions"
      (let [f (test-func #(throw (Exception. "")) 0)]
        (is (thrown-with-msg? Exception #"" (error/with-timeout 10000 (f))))
        (is (= 0 (error/with-timeout 10000 (f))))))
    (testing "with-timeout obeys the timeout"
      (let [f (test-func #(do (Thread/sleep 20)
                              libssh2/ERROR_EAGAIN)
                         0)]
        (is (thrown? Exception (error/with-timeout 10 (f))))))
    (testing "with-timeout can deal with fast-returning functions."
      (let [f (constantly libssh2/ERROR_EAGAIN)]
        (is (thrown? Exception (error/with-timeout 100 (f))))))
    (testing "with-timeout can use symbolic times"
      (with-redefs [error/timeouts (atom {:sym 5000})]
        (let [f (test-func libssh2/ERROR_EAGAIN 1)]
          (is (= 1 (error/with-timeout :sym (f)))))))))
