(ns clj-libssh2.socket
  (:require [net.n01se.clojure-jna :as jna]))

(def close
  "Close a socket"
  (jna/to-fn Integer simplesocket/simple_socket_close))

(defn connect
  "Create a socket and connect it to the given address and port."
  [address port]
  (let [socket (jna/invoke Integer
                           simplesocket/simple_socket_connect
                           address
                           port)]
    (when (> 0 socket)
      ;; Magic numbers are from libsimplesocket.h
      (throw (Exception. (case socket
                           -1 (format "%s is not a valid IP address" address)
                           -2 "Failed to create a TCP socket"
                           -3 (format "Failed to connect to %s:%d" address port)
                           "An unknown error occurred"))))
    socket))
