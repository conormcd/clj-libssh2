(ns clj-libssh2.channel
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.error :refer [handle-errors]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.channel :as libssh2-channel]
            [clj-libssh2.socket :refer [block block-return wait]])
  (:import [java.io InputStream OutputStream PushbackInputStream]
           [com.sun.jna.ptr IntByReference PointerByReference]))

(defn close
  "Close a channel."
  [session channel]
  (block session
    (handle-errors session (libssh2-channel/close channel))))

(defn exec
  "Execute a command via a channel."
  [session channel commandline]
  (block session
    (handle-errors session (libssh2-channel/exec channel commandline))))

(defn exit-signal
  "Collect the exit signal data from a channel."
  [session channel]
  (let [->str (fn [string-ref length-ref]
                (let [string (.getValue string-ref)
                      length (.getValue length-ref)]
                  (when (< 0 length)
                    (String. (.getByteArray string 0 length) "ASCII"))))
        exit-signal (PointerByReference.)
        exit-signal-len (IntByReference.)
        err-msg (PointerByReference.)
        err-msg-len (IntByReference.)
        lang-tag (PointerByReference.)
        lang-tag-len (IntByReference.)]
    (handle-errors session
      (libssh2-channel/get-exit-signal channel
                                       exit-signal exit-signal-len
                                       err-msg err-msg-len
                                       lang-tag lang-tag-len))
    {:exit-signal (->str exit-signal exit-signal-len)
     :err-msg (->str err-msg err-msg-len)
     :lang-tag (->str lang-tag lang-tag-len)}))

(defn exit-status
  "Get the exit code from the last executed command."
  [channel]
  (libssh2-channel/get-exit-status channel))

(defn free
  "Free a channel."
  [channel]
  (libssh2-channel/free channel))

(defn open
  "Create a new channel in a session."
  [session]
  (block-return session
    (libssh2-channel/open-session (:session session))))

(defn send-eof
  "Tell the remote process that we're done sending input."
  [session channel]
  (block session (handle-errors session (libssh2-channel/send-eof channel))))

(defn pull
  "Read some output from a given stream on a channel.

   Parameters:

   session       The usual session object.
   channel       A valid channel for this session.
   ssh-stream-id 0 for STDOUT, 1 for STDERR or any other number that the
                 process on the other end wishes to send data on.
   output-stream A java.io.OutputStream to send the data to.

   Return:

   Either :eof, :eagain or :ready. If :eof, then no more data will be sent. If
   :eagain, then more data might be available. You should select on the
   appropriate socket and try again. If :ready, the stream is ready for another
   read immediately."
  [session channel ssh-stream-id ^OutputStream output-stream]
  (let [size (-> session :options :read-chunk-size)
        buf1 (jna/make-cbuf size)
        res (handle-errors session
              (libssh2-channel/read-ex channel ssh-stream-id buf1 size))]
    (when (and (some? output-stream) (< 0 res))
      (let [buf2 (byte-array size (byte 0))]
        (.get buf1 buf2 0 res)
        (.write output-stream buf2 0 res)))
    (condp = res
      0 :eof
      libssh2/ERROR_EAGAIN :eagain
      :ready)))

(defn push
  "Write some input to a given stream on a channel.

   Parameters:

   session       The usual session object.
   channel       A valid channel for this session.
   ssh-stream-id 0 for STDIN or any other number that the process on the other
                 end wishes to receive data on.
   input-stream  A java.io.PushbackInputStream to grab the data from. This must
                 be capable of pushing back as `:write-chunk-size` bytes.

   Return:

   Either :eof or :ready. If :eof, then the input-stream has returned -1 and no
   more data will be read from it nor written to the channel. If :eagain, then
   you should select on the appropriate socket before calling this again.  If
   :ready then there are more bytes to be processed and this function should be
   called again."
  [session channel ssh-stream-id ^PushbackInputStream input-stream]
  {:pre [(instance? PushbackInputStream input-stream)]}
  (let [size (-> session :options :write-chunk-size)
        read-size (min size (-> session :options :read-chunk-size))
        buf (byte-array read-size (byte 0))
        bytes-read (.read input-stream buf)]
    (if (< -1 bytes-read)
      (if (< 0 bytes-read)
        (let [sent (handle-errors session
                     (libssh2-channel/write-ex channel ssh-stream-id buf size))]
          (when (< sent bytes-read)
            (let [bytes-sent (if (< 0 sent) sent 0)]
              (.unread input-stream buf bytes-sent (- bytes-read bytes-sent))))
          (if (= libssh2/ERROR_EAGAIN sent)
            :eagain
            :ready))
        :ready)
      (do
        (send-eof session channel)
        :eof))))

(defn- ensure-pushback
  [size stream]
  (let [s (:stream stream)]
    (if (and (instance? InputStream s) (not (instance? PushbackInputStream s)))
      (assoc stream :stream (PushbackInputStream. s size))
      stream)))

(defn- make-stream
  [now direction [id stream]]
  (hash-map :id id
            :direction direction
            :stream stream
            :last-read-time now
            :status :ready))

(defn- pump-stream
  "Do exactly one push/pull on a stream and enforce read timeouts"
  [session channel stream]
  (if (not= :eof (:status stream))
    (let [pump-fn (if (= :output (:direction stream)) pull push)
          last-read-time (:last-read-time stream)
          new-status (pump-fn session channel (:id stream) (:stream stream))
          now (System/currentTimeMillis)]
      (when (and (= pump-fn pull)
                 (= :eagain new-status)
                 (< (-> session :options :read-timeout) (- now last-read-time)))
        (throw (Exception. (format "Read timeout for %s stream %d"
                                   (-> stream :direction name)
                                   (-> stream :id)))))
      (assoc stream :status new-status :last-read-time now))
    stream))

(defn pump
  "Process a collection of input and output streams all at once."
  [session channel input-streams output-streams]
  (let [now (System/currentTimeMillis)
        write-size (-> session :options :write-chunk-size)
        streams (concat (->> input-streams
                             (map (partial make-stream now :input))
                             (map (partial ensure-pushback write-size)))
                        (->> output-streams
                             (map (partial make-stream now :output))))]
    (when-not (empty? streams)
      (loop [s (map (partial pump-stream session channel) streams)]
        (let [status-set (->> s (map :status) set)]
          (if (not= #{:eof} status-set)
            (do
              (when (contains? status-set :eagain)
                (wait session))
              (recur (map (partial pump-stream session channel) streams)))
            (->> s
                 (filter #(= :output (:direction %)))
                 (map #(hash-map (:id %) %))
                 (apply merge))))))))

(defmacro with-channel
  "Convenience macro for wrapping a bunch of channel operations."
  [session chan & body]
  `(let [~chan (open ~session)]
     (try
       (do ~@body)
       (finally
         (close ~session ~chan)
         (handle-errors ~session
           (free ~chan))))))
