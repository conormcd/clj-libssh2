(ns clj-libssh2.test-known-hosts
  (:require [clojure.test :refer :all]
            [clj-libssh2.session :as session]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(defn- session-with-options
  [options]
  (session/open test/ssh-host
                test/ssh-port
                {:username (test/ssh-user)}
                options))

(deftest by-default-we-don't-fail-if-the-host-is-unknown
  (is (= 0 (count @session/sessions)))
  (let [file (test/known-hosts-file :missing)
        session (session-with-options {:known-hosts-file file})]
    (is (= 1 (count @session/sessions)))
    (session/close session))
  (is (= 0 (count @session/sessions))))

(deftest by-default-we-fail-if-the-host-is-different
  (is (= 0 (count @session/sessions)))
  (is (thrown? Exception
               (let [file (test/known-hosts-file :bad)
                     session (session-with-options {:known-hosts-file file})]
                 (session/close session))))
  (is (= 0 (count @session/sessions))))

(deftest known-hosts-checking-works-when-the-host-is-known
  (is (= 0 (count @session/sessions)))
  (let [file (test/known-hosts-file :good)
        session (session-with-options {:fail-if-not-in-known-hosts true
                                       :fail-unless-known-hosts-matches true
                                       :known-hosts-file file})]
    (is (= 1 (count @session/sessions)))
    (session/close session))
  (is (= 0 (count @session/sessions))))

(deftest known-host-checking-can-be-ignored
  (doseq [known-hosts-file [:good :bad :missing]]
    (is (= 0 (count @session/sessions)))
    (let [file (test/known-hosts-file known-hosts-file)
          session (session-with-options {:fail-if-not-in-known-hosts false
                                         :fail-unless-known-hosts-matches false
                                         :known-hosts-file file})]
      (is (= 1 (count @session/sessions)))
      (session/close session))
    (is (= 0 (count @session/sessions)))))
