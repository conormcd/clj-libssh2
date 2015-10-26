(ns clj-libssh2.libssh2.channel
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.libssh2 :as libssh2])
  (:import [com.sun.jna Pointer]))

; int libssh2_channel_close(LIBSSH2_CHANNEL *channel);
(def close (jna/to-fn Integer ssh2/libssh2_channel_close))

; LIBSSH2_CHANNEL * libssh2_channel_direct_tcpip_ex(LIBSSH2_SESSION *session,
;                                                   const char *host,
;                                                   int port,
;                                                   const char *shost,
;                                                   int sport);
(def direct-tcpip-ex (jna/to-fn Pointer ssh2/libssh2_channel_direct_tcpip_ex))

; LIBSSH2_CHANNEL * libssh2_channel_direct_tcpip(LIBSSH2_SESSION *session,
;                                                const char *host,
;                                                int port);
(defn direct-tcpip
  [session host port]
  (direct-tcpip-ex session host port "127.0.0.1" 22))

; int libssh2_channel_eof(LIBSSH2_CHANNEL *channel);
(def eof (jna/to-fn Integer ssh2/libssh2_channel_eof))

; int libssh2_channel_exec(LIBSSH2_CHANNEL *channel, const char *command);
(declare process-startup)
(defn exec
  [channel command]
  (process-startup channel
                   "exec"
                   (count "shell")
                   command
                   (count command)))

; int libssh2_channel_flush_ex(LIBSSH2_CHANNEL *channel, int streamid);
(def flush-ex (jna/to-fn Integer ssh2/libssh2_channel_flush_ex))

; int libssh2_channel_flush(LIBSSH2_CHANNEL *channel);
(defn flush
  [channel]
  (flush-ex channel 0))

; int libssh2_channel_flush_stderr(LIBSSH2_CHANNEL *channel);
(defn flush-stderr
  [channel]
  (flush-ex channel libssh2/SSH_EXTENDED_DATA_STDERR))

; LIBSSH2_CHANNEL * libssh2_channel_forward_accept(LIBSSH2_LISTENER *listener);
(def forward-accept (jna/to-fn Pointer ssh2/libssh2_channel_forward_accept))

; int libssh2_channel_forward_cancel(LIBSSH2_LISTENER *listener);
(def forward-cancel (jna/to-fn Integer ssh2/libssh2_channel_forward_cancel))

; LIBSSH2_LISTENER * libssh2_channel_forward_listen_ex(LIBSSH2_SESSION *session,
;                                                      char *host,
;                                                      int port,
;                                                      int *bound_port,
;                                                      int queue_maxsize);
(def forward-listen-ex (jna/to-fn Pointer ssh2/libssh2_channel_forward_listen_ex))

; LIBSSH2_LISTENER * libssh2_channel_forward_listen(LIBSSH2_SESSION *session,
;                                                   int port);
(defn forward-listen
  [session port]
  (forward-listen-ex session nil port nil 16))

; int libssh2_channel_free(LIBSSH2_CHANNEL *channel);
(def free (jna/to-fn Integer ssh2/libssh2_channel_free))

; int libssh2_channel_get_exit_signal(LIBSSH2_CHANNEL *channel,
;                                     char **exitsignal,
;                                     size_t *exitsignal_len,
;                                     char **errmsg,
;                                     size_t *errmsg_len,
;                                     char **langtag,
;                                     size_t *langtag_len);
(def get-exit-signal (jna/to-fn Integer ssh2/libssh2_channel_get_exit_signal))

; int libssh2_channel_get_exit_status(LIBSSH2_CHANNEL* channel)
(def get-exit-status (jna/to-fn Integer ssh2/libssh2_channel_get_exit_status))

; void libssh2_channel_handle_extended_data(LIBSSH2_CHANNEL *channel,
;                                           int ignore_mode);
(def handle-extended-data (jna/to-fn Void ssh2/libssh2_channel_handle_extended_data))

; int libssh2_channel_handle_extended_data2(LIBSSH2_CHANNEL *channel,
;                                           int ignore_mode);
(def handle-extended-data2 (jna/to-fn Integer ssh2/libssh2_channel_handle_extended_data2))

; libssh2_channel_ignore_extended_data(arguments)
(defn ignore-extended-data
  [channel ignore]
  (handle-extended-data channel (if ignore
                                  libssh2/CHANNEL_EXTENDED_DATA_IGNORE
                                  libssh2/CHANNEL_EXTENDED_DATA_NORMAL)))

; LIBSSH2_CHANNEL * libssh2_channel_open_ex(LIBSSH2_SESSION *session,
;                                           const char *channel_type,
;                                           unsigned int channel_type_len,
;                                           unsigned int window_size,
;                                           unsigned int packet_size,
;                                           const char *message,
;                                           unsigned int message_len);
(def open-ex (jna/to-fn Pointer ssh2/libssh2_channel_open_ex))

; LIBSSH2_CHANNEL * libssh2_channel_open_session(LIBSSH2_SESSION *session);
(defn open-session
  [session]
  (open-ex session
           "session"
           (count "session")
           libssh2/CHANNEL_WINDOW_DEFAULT
           libssh2/CHANNEL_PACKET_DEFAULT
           nil
           0))

; int libssh2_channel_process_startup(LIBSSH2_CHANNEL *channel,
;                                     const char *request,
;                                     unsigned int request_len,
;                                     const char *message,
;                                     unsigned int message_len);
(def process-startup (jna/to-fn Integer ssh2/libssh2_channel_process_startup))

; ssize_t libssh2_channel_read_ex(LIBSSH2_CHANNEL *channel,
;                                 int stream_id,
;                                 char *buf,
;                                 size_t buflen);
(def read-ex (jna/to-fn Long ssh2/libssh2_channel_read_ex))

; ssize_t libssh2_channel_read(LIBSSH2_CHANNEL *channel,
;                              char *buf,
;                              size_t buflen);
(defn read
  [channel buf buflen]
  (read-ex channel 0 buf buflen))

; ssize_t libssh2_channel_read_stderr(LIBSSH2_CHANNEL *channel,
;                                     char *buf,
;                                     size_t buflen);
(defn read-stderr
  [channel buf buflen]
  (read-ex channel libssh2/SSH_EXTENDED_DATA_STDERR buf buflen))
;
; unsigned long libssh2_channel_receive_window_adjust(LIBSSH2_CHANNEL * channel,
;                                                     unsigned long adjustment,
;                                                     unsigned char force);
(def receive-window-adjust (jna/to-fn Long ssh2/libssh2_channel_receive_window_adjust))

; int libssh2_channel_receive_window_adjust2(LIBSSH2_CHANNEL * channel,
;                                            unsigned long adjustment,
;                                            unsigned char force,
;                                            unsigned int *window);
(def receive-window-adjust2 (jna/to-fn Integer ssh2/libssh2_channel_receive_window_adjust2))

; int libssh2_channel_request_pty_ex(LIBSSH2_CHANNEL *channel,
;                                    const char *term,
;                                    unsigned int term_len,
;                                    const char *modes,
;                                    unsigned int modes_len,
;                                    int width,
;                                    int height,
;                                    int width_px,
;                                    int height_px);
(def request-pty-ex (jna/to-fn Integer ssh2/libssh2_channel_request_pty_ex))

; int libssh2_channel_request_pty(LIBSSH2_CHANNEL *channel, char *term);
(defn request-pty
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

; LIBSSH2_API int libssh2_channel_request_pty_size_ex(LIBSSH2_CHANNEL *channel,
;                                                     int width,
;                                                     int height,
;                                                     int width_px,
;                                                     int height_px);
(def request-pty-size-ex (jna/to-fn Integer ssh2/libssh2_channel_request_pty_size_ex))

; int libssh2_channel_request_pty_size(LIBSSH2_CHANNEL *channel,
;                                      int width,
;                                      int height);
(defn request-pty-size
  [channel width height]
  (request-pty-ex channel width height 0 0))

; int libssh2_channel_send_eof(LIBSSH2_CHANNEL *channel);
(def send-eof (jna/to-fn Integer ssh2/libssh2_channel_send_eof))

; void libssh2_channel_set_blocking(LIBSSH2_CHANNEL *channel, int blocking);
(def set-blocking (jna/to-fn Void ssh2/libssh2_channel_set_blocking))

; int libssh2_channel_setenv_ex(LIBSSH2_CHANNEL *channel,
;                               char *varname,
;                               unsigned int varname_len,
;                               const char *value,
;                               unsigned int value_len);
(def setenv-ex (jna/to-fn Integer ssh2/libssh2_channel_setenv_ex))

; int libssh2_channel_setenv(LIBSSH2_CHANNEL *channel,
;                            char *varname,
;                            const char *value);
(defn setenv
  [channel varname value]
  (setenv-ex channel varname (count varname) value (count value)))

; int libssh2_channel_shell(LIBSSH2_CHANNEL *channel);
(defn shell
  [channel]
  (process-startup channel
                   "shell"
                   (count "shell")
                   nil
                   0))

; int libssh2_channel_subsystem(LIBSSH2_CHANNEL *channel, const char *subsystem);
(defn subsystem
  [channel subsystem]
  (process-startup channel
                   "subsystem"
                   (count "subsystem")
                   subsystem
                   (count subsystem)))

; int libssh2_channel_wait_closed(LIBSSH2_CHANNEL *channel);
(def wait-closed (jna/to-fn Integer ssh2/libssh2_channel_wait_closed))

; int libssh2_channel_wait_eof(LIBSSH2_CHANNEL *channel);
(def wait-eof (jna/to-fn Integer ssh2/libssh2_channel_wait_eof))

; unsigned long libssh2_channel_window_read_ex(LIBSSH2_CHANNEL *channel,
;                                              unsigned long *read_avail,
;                                              unsigned long *window_size_initial)
(def window-read-ex (jna/to-fn Long ssh2/libssh2_channel_window_read_ex))

; unsigned long libssh2_channel_window_read(LIBSSH2_CHANNEL *channel);
(defn window-read
  [channel]
  (window-read-ex channel nil nil))

; unsigned long libssh2_channel_window_write_ex(LIBSSH2_CHANNEL *channel,
;                                               unsigned long *window_size_initial)
(def window-write-ex (jna/to-fn Long ssh2/libssh2_channel_window_write_ex))

; unsigned long libssh2_channel_window_write(LIBSSH2_CHANNEL *channel);
(defn window-write
  [channel]
  (window-write-ex channel nil))

; ssize_t libssh2_channel_write_ex(LIBSSH2_CHANNEL *channel,
;                                  int stream_id, char *buf,
;                                  size_t buflen);
(def write-ex (jna/to-fn Long ssh2/libssh2_channel_write_ex))

; ssize_t libssh2_channel_write(LIBSSH2_CHANNEL *channel,
;                               const char *buf,
;                               size_t buflen);
(defn write
  [channel buf buflen]
  (write-ex channel 0 buf buflen))

; ssize_t libssh2_channel_write_stderr(LIBSSH2_CHANNEL *channel,
;                                      const char *buf,
;                                      size_t buflen);
(defn write-stderr
  [channel buf buflen]
  (write-ex channel libssh2/SSH_EXTENDED_DATA_STDERR buf buflen))

; int libssh2_channel_x11_req_ex(LIBSSH2_CHANNEL *channel, int single_connection, const char *auth_proto, const char *auth_cookie, int screen_number);
(def x11-req-ex (jna/to-fn Integer ssh2/libssh2_channel_x11_req_ex))

; int libssh2_channel_x11_req(LIBSSH2_CHANNEL *channel, int screen_number);
(defn x11-req
  [channel screen-number]
  (x11-req-ex channel 0 nil nil screen-number))
