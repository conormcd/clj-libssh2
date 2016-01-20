(ns clj-libssh2.channel
  "Functions for manipulating channels within an SSH session."
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.error :refer [handle-errors]]
            [clj-libssh2.libssh2 :as libssh2]
            [clj-libssh2.libssh2.channel :as libssh2-channel]
            [clj-libssh2.socket :refer [block block-return wait]])
  (:import [java.io InputStream PushbackInputStream]
           [com.sun.jna.ptr IntByReference PointerByReference]))

(defn close
  "Close a channel.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  The channel to close.

   Return:

   0 on success. An exception will be thrown if an error occurs."
  [session channel]
  (block session
    (handle-errors session (libssh2-channel/close channel))))

(defn exec
  "Execute a command on the remote host. This merely starts the execution of
   the command. The input(s), output(s) and exit status of the command must be
   handled separately.

   Arguments:

   session      The clj-libssh2.session.Session object for the current session.
   channel      Execute the command via this channel.
   commandline  The command to execute on the remote host.

   Return:

   0 on success. An exception will be thrown if an error occurs."
  [session channel commandline]
  (block session
    (handle-errors session (libssh2-channel/exec channel commandline))))

(defn exit-signal
  "Collect the exit signal data from a channel. This will only return the
   correct result after the command has completed so between calling exec and
   calling this function you should sleep, wait or read the command's input as
   appropriate.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  The channel on which a command was executed and has exited.

   Return:

   A map with the following keys and values:

   :exit-signal The name of the signal (without the leading \"SIG\").
   :err-msg     The error message.
   :lang-tag    The language tag, if provided."
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
  "Get the exit code from the last executed command. This will only return the
   correct result after the command has completed so between calling exec and
   calling this function you should sleep, wait or read the command's input as
   appropriate.

   Arguments:

   channel  The channel on which a command was executed and has exited.

   Return:

   The numeric exit code from the remote process."
  [channel]
  (libssh2-channel/get-exit-status channel))

(defn free
  "Free a native channel object. Be careful to only call this once. This will
   implicitly call close if the channel has not already been closed.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  The native channel object to free.

   Return:

   0 on success or throws an exception on failure."
  [session channel]
  (block session (handle-errors session (libssh2-channel/free channel))))

(defn open
  "Create a new channel for a session.

   Arguments:

   session The clj-libssh2.session.Session object for the current session.

   Return:

   A newly-allocated channel object, or throws exception on failure."
  [session]
  (block-return session (libssh2-channel/open-session (:session session))))

(defn send-eof
  "Tell the remote process that we won't send any more input.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  The native channel object on which we have finished sending data.

   Return:

   0 on success, throws an exception on failure."
  [session channel]
  (block session (handle-errors session (libssh2-channel/send-eof channel))))

(defn pull
  "Read some output from a given stream on a channel.

   This should probably only be called from pump.

   Arguments:

   session       The clj-libssh2.session.Session object for the current
                 session.
   channel       A valid channel for this session.
   ssh-stream-id 0 for STDOUT, 1 for STDERR or any other number that the
                 process on the other end wishes to send data on.
   output-stream A java.io.OutputStream to send the data to.

   Return:

   Either :eof, :eagain or :ready. If :eof, then no more data will be sent. If
   :eagain, then more data might be available. You should select on the
   appropriate socket and try again. If :ready, the stream is ready for another
   read immediately."
  [session channel ssh-stream-id output-stream]
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

   This should probably only be called from pump.

   Arguments:

   session       The clj-libssh2.session.Session object for the current
                 session.
   channel       A valid channel for this session.
   ssh-stream-id 0 for STDIN or any other number that the process on the other
                 end wishes to receive data on.
   input-stream  A java.io.PushbackInputStream to grab the data from. This must
                 be capable of pushing back at least :write-chunk-size bytes.

   Return:

   Either :eof or :ready. If :eof, then the input-stream has returned -1 and no
   more data will be read from it nor written to the channel. If :eagain, then
   you should select on the appropriate socket before calling this again.  If
   :ready then there are more bytes to be processed and this function should be
   called again."
  [session channel ssh-stream-id input-stream]
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
  "Ensure that the given stream is a PushbackInputStream if it's an
   InputStream. This is a helper for use in pump.

   Arguments:

   size   The size of the PushbackInputStream's internal pushback buffer. This
          must be greater than or equal to the session's :write-chunk-size or
          else short writes may not be able to push back enough unsent data.
   stream The stream to (potentially) modify.

   Return:

   An instance of PushbackInputStream."
  [size stream]
  (let [s (:stream stream)]
    (if (and (instance? InputStream s) (not (instance? PushbackInputStream s)))
      (assoc stream :stream (PushbackInputStream. s size))
      stream)))

(defn- make-stream
  "Expand an [ID InputStream/OutputStream] pair into a map with more useful
   information about the stream. This is a helper for use in pump.

   Arguments:

   now              The current time in milliseconds.
   stream-map-entry A single entry from a map of SSH stream IDs to the
                    InputStream/OutputStream objects which will feed or be fed
                    from that SSH stream.

   Return:

   A map with the following keys and values.

   :direction       The direction of the stream. :input for an InputStream,
                    :output for an OutputStream.
   :id              The ID of the SSH stream we're reading from/writing to.
   :last-read-time  The last time the stream has been read. Set to now in this
                    function and updated in pump-stream.
   :status          Either :ready, :eof or :eagain. Set to :ready here.
   :stream          The InputStream/OutputStream object connected to the SSH
                    stream."
  [now direction [id stream]]
  (hash-map :id id
            :direction (if (instance? InputStream stream) :input :output)
            :stream stream
            :last-read-time now
            :status :ready))

(defn- pump-stream
  "Do exactly one push/pull on a stream and enforce read timeouts. This is a
   helper for use in pump.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  The SSH channel that we're doing IO on.
   stream   A stream map as returned by make-stream.

   Return:

   The stream argument, updated with a new :status and :last-read-time if
   appropriate."
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
  "Process a collection of input and output streams all at once. This will run
   until all streams have reported EOF.

   Arguments:

   session        The clj-libssh2.session.Session object for the current
                  session.
   channel        The SSH channel that we're doing IO on.
   input-streams  A map where the keys are the SSH stream IDs and the values
                  are the InputStream objects used to feed those IDs.
   output-streams A map where the keys are the SSH stream IDs and the values
                  are the OutputStream objects used to read from those IDs.

   Return:

   A map where the keys are the SSH output stream IDs and the values are maps
   with the following keys:

   :direction       Always :output
   :id              The ID of the SSH stream we're reading from.
   :last-read-time  The last time the stream has been read.
   :status          Always :eof
   :stream          The OutputStream object connected to the SSH stream."
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
  "Convenience macro for wrapping a bunch of channel operations.

   Arguments:

   session  The clj-libssh2.session.Session object for the current session.
   channel  This will be bound to the result of a call to open."
  [session channel & body]
  `(let [~channel (open ~session)]
     (try
       (do ~@body)
       (finally
         (close ~session ~channel)
         (free ~session ~channel)))))
