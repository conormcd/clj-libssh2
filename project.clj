(defproject clj-libssh2 "0.1.0-SNAPSHOT"
  :description "A Clojure wrapper around libssh2"
  :url "https://github.com/conormcd/clj-libssh2"
  :license {:name "BSD"
            :url "https://github.com/conormcd/clj-libssh2/blob/master/LICENSE"}
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [net.n01se/clojure-jna "1.0.0"]]
  :deploy-repositories ^:replace [["clojars" {:url "https://clojars.org/repo"
                                              :username [:gpg :env/clojars_username]
                                              :password [:gpg :env/clojars_password]
                                              :sign-releases false}]]
  :jvm-opts ["-Xmx1g"
             "-XX:+TieredCompilation"
             "-XX:TieredStopAtLevel=1"])
