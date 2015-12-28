(ns clj-libssh2.test-utils
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.test :as test]
            [net.n01se.clojure-jna :as jna]))

(def ssh-host "127.0.0.1")
(def ssh-port 2222)
(defn ssh-user [] (System/getProperty "user.name"))

(defn test-script
  [script]
  (str/join "/" [(System/getProperty "user.dir") "test/script" script]))

(defn run-test-script
  [script & args]
  (let [result (apply sh/sh "sh" (test-script script) (map str args))]
    (when-not (= 0 (:exit result))
      (throw (Exception. (:err result))))
    result))

(defn setenv
  "Change the environment. Only the C libraries will be able to see this change
   since Java caches the environement on startup."
  [varname value]
  (when-not (= 0 (jna/invoke Integer c/setenv varname value 1))
    (throw (Exception. (format "Failed to setenv %s=%s" varname value)))))

(defn setup
  []
  (let [result (run-test-script "setup.sh" ssh-host ssh-port)
        env (->> result
                 :out
                 str/split-lines
                 (map #(str/split % #"=" 2)))]
    (doseq [[k v] env]
      (setenv k v))))

(defn teardown
  []
  (run-test-script "teardown.sh"))

(defn with-sandbox-sshd
  [f]
  (setup)
  (f)
  (teardown))

(defn fixtures
  []
  (test/use-fixtures :once with-sandbox-sshd))
