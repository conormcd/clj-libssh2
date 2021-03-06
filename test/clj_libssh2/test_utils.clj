(ns clj-libssh2.test-utils
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.test :as test :refer [is]]
            [clojure-test-junit-output.core :refer [with-junit-output]]
            [net.n01se.clojure-jna :as jna]
            [clj-libssh2.logging :as logging] 
            [clj-libssh2.session :as session])
  (:import [java.io File]))

(def ssh-host "127.0.0.1")
(def ssh-port 2222)
(defn ssh-user [] (System/getProperty "user.name"))

(defmacro with-test-session
  [session session-params & body]
  `(let [session-params# ~session-params
         params# (merge {:hostname (or (:hostname session-params# ssh-host))
                         :port (or (:port session-params#) ssh-port)
                         :credentials (merge {:username (ssh-user)}
                                             (:credentials session-params#))}
                        (dissoc session-params# :hostname :port :credentials))]
     (session/with-session session# params#
       ~@body)))

(defn auth
  ([]
   (auth {}))
  ([session-params]
   (is (= 0 (count @session/sessions)))
   (with-test-session session session-params
     (is (= 1 (count @session/sessions))))
   (is (= 0 (count @session/sessions)))))

(defn test-script
  [script]
  (str/join "/" [(System/getProperty "user.dir") "test/script" script]))

(defn known-hosts-file
  [variant]
  (str/join "/" [(System/getProperty "user.dir") "test/tmp" (str "known_hosts_" (name variant))]))

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

(defn with-really-verbose-logging
  [f]
  (logging/init)
  (f))

(defn fixtures
  []
  (test/use-fixtures :once (test/join-fixtures
                             [(with-junit-output "test/junit.xml")
                              with-sandbox-sshd
                              with-really-verbose-logging])))

(defmacro with-temp-file
  [file & body]
  `(let [file# (File/createTempFile "clj-libssh2" nil)
         ~file (.getPath file#)]
     (try
       (do ~@body)
       (finally (.delete file#)))))
