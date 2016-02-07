(defproject clj-libssh2 "0.1.0-SNAPSHOT"
  :description "A Clojure wrapper around libssh2"
  :url "https://github.com/conormcd/clj-libssh2"
  :license {:name "BSD"
            :url "https://github.com/conormcd/clj-libssh2/blob/master/LICENSE"}
  :pedantic? :abort
  :java-source-paths ["src-java"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [digest "1.4.4"]
                 [net.n01se/clojure-jna "1.0.0"]
                 [robert/hooke "1.3.0"]]
  :profiles {:dev {:plugins [[lein-codox "0.9.1"]]}
             :test {:jvm-opts ["-Djava.util.logging.config.file=test/logging.properties"]}}
  :deploy-repositories ^:replace [["clojars" {:url "https://clojars.org/repo"
                                              :username [:gpg :env/clojars_username]
                                              :password [:gpg :env/clojars_password]
                                              :sign-releases false}]]
  :codox {:output-path "doc/api"}
  :jvm-opts ["-Xmx1g"
             "-XX:+TieredCompilation"
             "-XX:TieredStopAtLevel=1"])
