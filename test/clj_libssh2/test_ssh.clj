(ns clj-libssh2.test-ssh
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [clj-libssh2.ssh :as ssh]
            [clj-libssh2.test-utils :as test]))

(test/fixtures)

(defn- test-ssh-exec
  "Run a simple command line that validates that STDOUT, STDERR and the exit
  code are properly passed back from the remote side."
  [session command-outputs expected & args]
  (let [stdout (first command-outputs)
        stderr (second command-outputs)
        exit-code (last command-outputs)
        expected-out (if expected (first expected) stdout)
        expected-err (if expected (second expected) stderr)
        expected-exit (if expected (last expected) exit-code)
        command (->> [(when stdout (format "echo %s" stdout))
                      (when stderr (format "echo %s 1>&2" stderr))
                      (when exit-code (format "exit %d" exit-code))]
                     (remove nil?)
                     (str/join "; "))
        result (apply ssh/exec session command args)]
    (is (= (some-> expected-out (str "\n")) (:out result)))
    (is (= (some-> expected-err (str "\n")) (:err result)))
    (is (= expected-exit (:exit result)))
    result))

(deftest exec-handles-sessions-correctly
  (testing "With a Session as a session"
    (testing "it can run a simple command"
      (ssh/with-session session {:port 2222}
        (test-ssh-exec session ["1" "2" 0] nil)))
    (testing "it can run multiple commands over the same session"
      (ssh/with-session session {:port 2222}
        (test-ssh-exec session ["1" "2" 0] nil)
        (test-ssh-exec session ["3" "4" 42] nil))))
  (testing "With a map as a session"
    (testing "it can run a simple command"
      (test-ssh-exec {:port 2222} ["1" "2" 0] nil))))

(deftest exec-works-when-we-ignore-output
  (ssh/with-session session {:port 2222}
    (testing "Commands will successfully run when we ignore output"
      (test-ssh-exec session ["1" "2" 3] [nil nil 3] :out nil :err nil))
    (testing "We can independently ignore stdout and stderr"
      (test-ssh-exec session ["1" "2" 3] [nil "2" 3] :out nil)
      (test-ssh-exec session ["1" "2" 3] ["1" nil 3] :err nil))))

(deftest exec-can-be-given-input-on-stdin
  (let [timeout-cat "while read -t 1 s; do echo $s; done"]
    (ssh/with-session session {:port 2222}
      (testing "Commands can be given some input on STDIN"
        (let [input "foo\nbar\n"
              result (ssh/exec session timeout-cat :in input)]
          (is (= input (:out result)))
          (is (= "" (:err result)))
          (is (= 0 (:exit result)))))
      (testing "Commands can be given a lot of input on STDIN"
        (let [input-line (str (str/join "" (repeat 1024 "x")) "\n")
              input (str/join "" (repeat (* 10 1024) input-line))
              result (ssh/exec session timeout-cat :in input)]
          (is (= (count input) (count (:out result))))
          (is (= "" (:err result)))
          (is (= 0 (:exit result))))))))

(deftest exec-can-be-given-environment-variables
  (testing "Exec can be given environment variables."
    (ssh/with-session session {:port 2222}
      (let [env {:FOO "foo" :BAR :bar :BAZ 1 :QUUX nil}
            result (ssh/exec session "env" :env env)
            out-env (->> result
                         :out
                         str/split-lines
                         (map #(str/split % #"=" 2))
                         (into {}))]
        (is (= "foo" (get out-env "FOO")))
        (is (= "bar" (get out-env "BAR")))
        (is (= "1" (get out-env "BAZ")))
        (is (= "" (get out-env "QUUX")))
        (is (= "" (:err result)))
        (is (= 0 (:exit result)))))))

(deftest exec-times-out-when-commands-take-too-long
  (testing "Commands that take too long result in a timeout"
    (is (thrown? Exception (ssh/exec {:port 2222 :read-timeout 500}
                                     "echo foo; sleep 1; echo bar")))))
