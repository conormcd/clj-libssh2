(ns clj-libssh2.socket
  (:require [clojure.tools.logging :as log]
            [net.n01se.clojure-jna :as jna]
            [clj-libssh2.error :as error :refer [handle-errors]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.keepalive :as libssh2-keepalive]
            [clj-libssh2.libssh2.session :as libssh2-session])
  (:import [com.sun.jna.ptr IntByReference]))

;; Constants from libsimplesocket.h

(def SIMPLE_SOCKET_BAD_ADDRESS -1)
(def SIMPLE_SOCKET_SOCKET_FAILED -2)
(def SIMPLE_SOCKET_CONNECT_FAILED -3)

(def SIMPLE_SOCKET_SELECT_READ 1)
(def SIMPLE_SOCKET_SELECT_WRITE 2)
(def SIMPLE_SOCKET_SELECT_ERROR 4)

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
      (let [message (condp = socket
                      SIMPLE_SOCKET_BAD_ADDRESS
                      (format "%s is not a valid IP address" address)

                      SIMPLE_SOCKET_SOCKET_FAILED
                      "Failed to create a TCP socket"

                      SIMPLE_SOCKET_CONNECT_FAILED
                      (format "Failed to connect to %s:%d" address port)

                      "simple_socket_connect returned a bad value")]
        (error/raise message {:socket socket})))
    socket))

(defn select
  "Call select on a socket from a clj-libssh2 Session."
  [session select-read select-write timeout]
  (log/debug "Calling select() on the socket.")
  (jna/invoke Integer
              simplesocket/simple_socket_select
              (:socket session)
              (bit-or
                (if select-read SIMPLE_SOCKET_SELECT_READ 0)
                (if select-write SIMPLE_SOCKET_SELECT_WRITE 0))
              timeout))

(defn send-keepalive
  "Send a keepalive message and return the number of seconds until the next
   time we should send a keepalive."
  [session]
  (log/debug "Sending a keepalive.")
  (let [seconds-to-wait (IntByReference.)]
    (handle-errors session
      (libssh2-keepalive/send (:session session) seconds-to-wait))
    (.getValue seconds-to-wait)))

(defn wait
  "Roughly equivalent to _libssh2_wait_socket in libssh2. Will raise an error
  on timeout or just block until it's time to try again."
  ([session]
   (wait session (System/currentTimeMillis)))
  ([session start-time]
   (when (and session (:session session) (> 0 (:socket session)))
     (let [ms-until-next-keepalive (* (send-keepalive session) 1000)
           block-directions (libssh2-session/block-directions (:session session))
           block-for-read (boolean (bit-and block-directions libssh2/SESSION_BLOCK_INBOUND))
           block-for-write (boolean (bit-and block-directions libssh2/SESSION_BLOCK_OUTBOUND))
           libssh2-timeout-ms (libssh2-session/get-timeout (:session session))
           select-until (partial select session block-for-read block-for-write)
           select-result (cond (and (< 0 libssh2-timeout-ms)
                                    (or (= 0 ms-until-next-keepalive)
                                        (> ms-until-next-keepalive libssh2-timeout-ms)))
                               (let [elapsed (- (System/currentTimeMillis) start-time)]
                                 (when (> elapsed libssh2-timeout-ms)
                                   (handle-errors session libssh2/ERROR_TIMEOUT))
                                 (select-until (- libssh2-timeout-ms elapsed)))

                               (< 0 ms-until-next-keepalive)
                               (select-until ms-until-next-keepalive)

                               :else
                               (select-until 0))]
       (when (>= 0 select-result)
         (handle-errors session libssh2/ERROR_TIMEOUT))))))

(defmacro block
  "Turn a non-blocking call that returns EAGAIN into a blocking one."
  [session timeout & body]
  `(let [session# ~session
         start-time# (System/currentTimeMillis)
         timeout# ~timeout]
     (while (= libssh2/ERROR_EAGAIN (do ~@body))
       (handle-errors session#
         (wait session# start-time#))
       (error/enforce-timeout session# start-time# timeout#))))

(defmacro block-return
  "Similar to block, but for functions that return a pointer"
  [session timeout & body]
  `(let [session# ~session
         start-time# (System/currentTimeMillis)
         timeout# ~timeout]
     (loop [result# (do ~@body)]
       (if (nil? result#)
         (let [errno# (libssh2-session/last-errno (:session session#))]
           (handle-errors session# errno#)
           (when (= libssh2/ERROR_EAGAIN errno#)
             (wait session# start-time#))
           (error/enforce-timeout session# start-time# timeout#)
           (recur (do ~@body)))
         result#))))
