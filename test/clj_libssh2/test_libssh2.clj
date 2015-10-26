(ns clj-libssh2.test-libssh2
  (:require [clojure.test :refer :all]
            [clj-libssh2.libssh2 :as libssh2]))

(deftest check-libssh2-version-is-ok
  "This shoudln't change by surprise!"
  (is (= "1.6.0" (libssh2/version))))
