(ns clj-libssh2.test-known-hosts
  (:require [clojure.test :refer :all]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(deftest by-default-we-don't-fail-if-the-host-is-unknown
  (test/auth {:known-hosts-file (test/known-hosts-file :missing)}))

(deftest by-default-we-fail-if-the-host-is-different
  (is (thrown? Exception
               (test/auth {:known-hosts-file (test/known-hosts-file :bad)}))))

(deftest known-hosts-checking-works-when-the-host-is-known
  (test/auth {:fail-if-not-in-known-hosts true
              :fail-unless-known-hosts-matches true
              :known-hosts-file (test/known-hosts-file :good)}))

(deftest known-host-checking-can-be-ignored
  (doseq [file (map test/known-hosts-file [:good :bad :missing])]
    (test/auth {:fail-if-not-in-known-hosts false
                :fail-unless-known-hosts-matches false
                :known-hosts-file file})))
