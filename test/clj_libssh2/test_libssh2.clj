(ns clj-libssh2.test-libssh2
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(deftest check-libssh2-version-is-ok
  "This shoudln't change by surprise!"
  (is (= "1.6.0" (libssh2/version))))
