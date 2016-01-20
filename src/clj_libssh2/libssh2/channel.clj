(ns clj-libssh2.libssh2.channel
  "JNA functions for calling functions in libssh2 with names libssh2_channel_*"
  (:refer-clojure :exclude [flush read])
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.libssh2 :as libssh2])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([channel])} close
  "int libssh2_channel_close(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_close))

(def ^{:arglists '([session host port shost sport])} direct-tcpip-ex
  "
   LIBSSH2_CHANNEL * libssh2_channel_direct_tcpip_ex(LIBSSH2_SESSION *session,
                                                     const char *host,
                                                     int port,
                                                     const char *shost,
                                                     int sport);"
  (jna/to-fn Pointer ssh2/libssh2_channel_direct_tcpip_ex))

(defn direct-tcpip
  "
   LIBSSH2_CHANNEL * libssh2_channel_direct_tcpip(LIBSSH2_SESSION *session,
                                                  const char *host,
                                                  int port);"
  [session host port]
  (direct-tcpip-ex session host port "127.0.0.1" 22))

(def ^{:arglists '([channel])} eof
  "int libssh2_channel_eof(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_eof))

(declare process-startup)
(defn exec
  "int libssh2_channel_exec(LIBSSH2_CHANNEL *channel, const char *command);"
  [channel command]
  (process-startup channel
                   "exec"
                   (count "shell")
                   command
                   (count command)))

(def ^{:arglists '([channel streamid])} flush-ex
  "int libssh2_channel_flush_ex(LIBSSH2_CHANNEL *channel, int streamid);"
  (jna/to-fn Integer ssh2/libssh2_channel_flush_ex))

(defn flush
  "int libssh2_channel_flush(LIBSSH2_CHANNEL *channel);"
  [channel]
  (flush-ex channel 0))

(defn flush-stderr
  "int libssh2_channel_flush_stderr(LIBSSH2_CHANNEL *channel);"
  [channel]
  (flush-ex channel libssh2/SSH_EXTENDED_DATA_STDERR))

(def ^{:arglists '([listener])} forward-accept
  "LIBSSH2_CHANNEL * libssh2_channel_forward_accept(LIBSSH2_LISTENER *listener);"
  (jna/to-fn Pointer ssh2/libssh2_channel_forward_accept))

(def ^{:arglists '([listener])} forward-cancel
  "int libssh2_channel_forward_cancel(LIBSSH2_LISTENER *listener);"
  (jna/to-fn Integer ssh2/libssh2_channel_forward_cancel))

(def ^{:arglists '([session host port bound-port queue-maxsize])} forward-listen-ex
  "
   LIBSSH2_LISTENER * libssh2_channel_forward_listen_ex(LIBSSH2_SESSION *session,
                                                        char *host,
                                                        int port,
                                                        int *bound_port,
                                                        int queue_maxsize);"
  (jna/to-fn Pointer ssh2/libssh2_channel_forward_listen_ex))

(defn forward-listen
  "LIBSSH2_LISTENER * libssh2_channel_forward_listen(LIBSSH2_SESSION *session,
                                                     int port);"
  [session port]
  (forward-listen-ex session nil port nil 16))

(def ^{:arglists '([channel])} free
  "int libssh2_channel_free(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_free))

(def ^{:arglists '([channel exit-signal exit-signal-len err-msg err-msg-len lang-tag lang-tag-len])} get-exit-signal
  "
   int libssh2_channel_get_exit_signal(LIBSSH2_CHANNEL *channel,
                                       char **exitsignal,
                                       size_t *exitsignal_len,
                                       char **errmsg,
                                       size_t *errmsg_len,
                                       char **langtag,
                                       size_t *langtag_len);"
  (jna/to-fn Integer ssh2/libssh2_channel_get_exit_signal))

(def ^{:arglists '([channel])} get-exit-status
  "int libssh2_channel_get_exit_status(LIBSSH2_CHANNEL* channel)"
  (jna/to-fn Integer ssh2/libssh2_channel_get_exit_status))

(def ^{:arglists '([channel ignore-mode]) :deprecated "libssh2"} handle-extended-data
  "
   void libssh2_channel_handle_extended_data(LIBSSH2_CHANNEL *channel,
                                             int ignore_mode);"
  (jna/to-fn Void ssh2/libssh2_channel_handle_extended_data))

(def ^{:arglists '([channel ignore-mode])} handle-extended-data2
  "
   int libssh2_channel_handle_extended_data2(LIBSSH2_CHANNEL *channel,
                                             int ignore_mode);"
  (jna/to-fn Integer ssh2/libssh2_channel_handle_extended_data2))

(defn ^{:deprecated "libssh2"} ignore-extended-data
  "
   void libssh2_channel_ignore_extended_data(LIBSSH2_CHANNEL *channel,
                                             int ignore);"
  [channel ignore]
  (handle-extended-data channel (if ignore
                                  libssh2/CHANNEL_EXTENDED_DATA_IGNORE
                                  libssh2/CHANNEL_EXTENDED_DATA_NORMAL)))

(def ^{:arglists '([session channel-type channel-type-len window-size packet-size message message-len])} open-ex
  "
   LIBSSH2_CHANNEL * libssh2_channel_open_ex(LIBSSH2_SESSION *session,
                                             const char *channel_type,
                                             unsigned int channel_type_len,
                                             unsigned int window_size,
                                             unsigned int packet_size,
                                             const char *message,
                                             unsigned int message_len);"
  (jna/to-fn Pointer ssh2/libssh2_channel_open_ex))

(defn open-session
  "LIBSSH2_CHANNEL * libssh2_channel_open_session(LIBSSH2_SESSION *session);"
  [session]
  (open-ex session
           "session"
           (count "session")
           libssh2/CHANNEL_WINDOW_DEFAULT
           libssh2/CHANNEL_PACKET_DEFAULT
           nil
           0))

(def ^{:arglists '([channel request request-len message message-len])} process-startup
  "
   int libssh2_channel_process_startup(LIBSSH2_CHANNEL *channel,
                                       const char *request,
                                       unsigned int request_len,
                                       const char *message,
                                       unsigned int message_len);"
  (jna/to-fn Integer ssh2/libssh2_channel_process_startup))

(def ^{:arglists '([channel stream-id buf buflen])} read-ex
  "
   ssize_t libssh2_channel_read_ex(LIBSSH2_CHANNEL *channel,
                                   int stream_id,
                                   char *buf,
                                   size_t buflen);"
  (jna/to-fn Long ssh2/libssh2_channel_read_ex))

(defn read
  "
   ssize_t libssh2_channel_read(LIBSSH2_CHANNEL *channel,
                                char *buf,
                                size_t buflen);"
  [channel buf buflen]
  (read-ex channel 0 buf buflen))

(defn read-stderr
  "
   ssize_t libssh2_channel_read_stderr(LIBSSH2_CHANNEL *channel,
                                       char *buf,
                                       size_t buflen);"
  [channel buf buflen]
  (read-ex channel libssh2/SSH_EXTENDED_DATA_STDERR buf buflen))

(def ^{:arglists '([channel adjustment force]) :deprecated "libssh2"} receive-window-adjust
  "
   unsigned long libssh2_channel_receive_window_adjust(LIBSSH2_CHANNEL * channel,
                                                       unsigned long adjustment,
                                                       unsigned char force);"
  (jna/to-fn Long ssh2/libssh2_channel_receive_window_adjust))

(def ^{:arglists '([channel adjustment force window])} receive-window-adjust2
  "
   int libssh2_channel_receive_window_adjust2(LIBSSH2_CHANNEL * channel,
                                              unsigned long adjustment,
                                              unsigned char force,
                                              unsigned int *window);"
  (jna/to-fn Integer ssh2/libssh2_channel_receive_window_adjust2))

(def ^{:arglists '([channel term term-len modes modes-len width height width-px height-px])} request-pty-ex
  "
   int libssh2_channel_request_pty_ex(LIBSSH2_CHANNEL *channel,
                                      const char *term,
                                      unsigned int term_len,
                                      const char *modes,
                                      unsigned int modes_len,
                                      int width,
                                      int height,
                                      int width_px,
                                      int height_px);"
  (jna/to-fn Integer ssh2/libssh2_channel_request_pty_ex))

(defn request-pty
  "int libssh2_channel_request_pty(LIBSSH2_CHANNEL *channel, char *term);"
  [channel term]
  (request-pty-ex channel
                  term
                  (count term)
                  nil
                  0
                  libssh2/TERM_WIDTH
                  libssh2/TERM_HEIGHT
                  libssh2/TERM_WIDTH_PX
                  libssh2/TERM_HEIGHT_PX))

(def ^{:arglists '([channel width height width-px height-px])} request-pty-size-ex
  "
   LIBSSH2_API int libssh2_channel_request_pty_size_ex(LIBSSH2_CHANNEL *channel,
                                                       int width,
                                                       int height,
                                                       int width_px,
                                                       int height_px);"
  (jna/to-fn Integer ssh2/libssh2_channel_request_pty_size_ex))

(defn request-pty-size
  "
   int libssh2_channel_request_pty_size(LIBSSH2_CHANNEL *channel,
                                        int width,
                                        int height);"
  [channel width height]
  (request-pty-ex channel width height 0 0))

(def ^{:arglists '([channel])} send-eof
  "int libssh2_channel_send_eof(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_send_eof))

(def ^{:arglists '([channel blocking])} set-blocking
  "void libssh2_channel_set_blocking(LIBSSH2_CHANNEL *channel, int blocking);"
  (jna/to-fn Void ssh2/libssh2_channel_set_blocking))

(def ^{:arglists '([channel varname varname-len value value-len])} setenv-ex
  "
   int libssh2_channel_setenv_ex(LIBSSH2_CHANNEL *channel,
                                 char *varname,
                                 unsigned int varname_len,
                                 const char *value,
                                 unsigned int value_len);"
  (jna/to-fn Integer ssh2/libssh2_channel_setenv_ex))

(defn setenv
  "
   int libssh2_channel_setenv(LIBSSH2_CHANNEL *channel,
                              char *varname,
                              const char *value);"
  [channel varname value]
  (setenv-ex channel varname (count varname) value (count value)))

(defn shell
  "int libssh2_channel_shell(LIBSSH2_CHANNEL *channel);"
  [channel]
  (process-startup channel "shell" (count "shell") nil 0))

(defn subsystem
  "
   int libssh2_channel_subsystem(LIBSSH2_CHANNEL *channel,
                                 const char *subsystem);"
  [channel subsystem]
  (process-startup channel
                   "subsystem"
                   (count "subsystem")
                   subsystem
                   (count subsystem)))

(def ^{:arglists '([channel])} wait-closed
  "int libssh2_channel_wait_closed(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_wait_closed))

(def ^{:arglists '([channel])} wait-eof
  "int libssh2_channel_wait_eof(LIBSSH2_CHANNEL *channel);"
  (jna/to-fn Integer ssh2/libssh2_channel_wait_eof))

(def ^{:arglists '([channel read-avail window-size-initial])} window-read-ex
  "
   unsigned long libssh2_channel_window_read_ex(LIBSSH2_CHANNEL *channel,
                                                unsigned long *read_avail,
                                                unsigned long *window_size_initial);"
  (jna/to-fn Long ssh2/libssh2_channel_window_read_ex))

(defn window-read
  "unsigned long libssh2_channel_window_read(LIBSSH2_CHANNEL *channel);"
  [channel]
  (window-read-ex channel nil nil))

(def ^{:arglists '([channel window-size-initial])} window-write-ex
  "
   unsigned long libssh2_channel_window_write_ex(LIBSSH2_CHANNEL *channel,
                                                 unsigned long *window_size_initial);"
  (jna/to-fn Long ssh2/libssh2_channel_window_write_ex))

(defn window-write
  "unsigned long libssh2_channel_window_write(LIBSSH2_CHANNEL *channel);"
  [channel]
  (window-write-ex channel nil))

(def ^{:arglists '([channel stream-id buf buflen])} write-ex
  "ssize_t libssh2_channel_write_ex(LIBSSH2_CHANNEL *channel,
                                   int stream_id, char *buf,
                                   size_t buflen);"
  (jna/to-fn Long ssh2/libssh2_channel_write_ex))

(defn write
  "
   ssize_t libssh2_channel_write(LIBSSH2_CHANNEL *channel,
                                 const char *buf,
                                 size_t buflen);"
  [channel buf buflen]
  (write-ex channel 0 buf buflen))

(defn write-stderr
  "
   ssize_t libssh2_channel_write_stderr(LIBSSH2_CHANNEL *channel,
                                        const char *buf,
                                        size_t buflen);"
  [channel buf buflen]
  (write-ex channel libssh2/SSH_EXTENDED_DATA_STDERR buf buflen))

(def ^{:arglists '([channel single-connection auth-proto auth-cookie screen-number])} x11-req-ex
  "
   int libssh2_channel_x11_req_ex(LIBSSH2_CHANNEL *channel,
                                  int single_connection,
                                  const char *auth_proto,
                                  const char *auth_cookie,
                                  int screen_number);"
  (jna/to-fn Integer ssh2/libssh2_channel_x11_req_ex))

(defn x11-req
  "int libssh2_channel_x11_req(LIBSSH2_CHANNEL *channel, int screen_number);"
  [channel screen-number]
  (x11-req-ex channel 0 nil nil screen-number))
