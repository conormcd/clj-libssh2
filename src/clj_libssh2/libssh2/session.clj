(ns clj-libssh2.libssh2.session
  (:refer-clojure :exclude [methods])
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.libssh2 :as libssh2])
  (:import [com.sun.jna Pointer]))

; void **libssh2_session_abstract(LIBSSH2_SESSION *session);
(def abstract (jna/to-fn Pointer ssh2/libssh2_session_abstract))

; const char *libssh2_session_banner_get(LIBSSH2_SESSION *session);
(def banner-get (jna/to-fn String ssh2/libssh2_session_banner_get))

; int libssh2_session_banner_set(LIBSSH2_SESSION *session,
;                                const char *banner);
(def banner-set (jna/to-fn Integer ssh2/libssh2_session_banner_set))

; int libssh2_session_block_directions(LIBSSH2_SESSION *session);
(def block-directions (jna/to-fn Integer ssh2/libssh2_session_block_directions))

; void *libssh2_session_callback_set(LIBSSH2_SESSION *session,
;                                    int cbtype,
;                                    void *callback);
(def callback-set (jna/to-fn Pointer ssh2/libssh2_session_callback_set))

; int libssh2_session_disconnect_ex(LIBSSH2_SESSION *session,
;                                   int reason,
;                                   const char *description,
;                                   const char *lang);
(def disconnect-ex (jna/to-fn Integer ssh2/libssh2_session_disconnect_ex))

; int libssh2_session_disconnect(LIBSSH2_SESSION *session,
;                                const char *description);
(defn disconnect
  [session description]
  (disconnect-ex session libssh2/SSH_DISCONNECT_BY_APPLICATION description ""))

; int libssh2_session_flag(LIBSSH2_SESSION *session, int flag, int value);
(def flag (jna/to-fn Integer ssh2/libssh2_session_flag))

; int libssh2_session_free(LIBSSH2_SESSION *session);
(def free (jna/to-fn Integer ssh2/libssh2_session_free))

; int libssh2_session_get_blocking(LIBSSH2_SESSION* session);
(def get-blocking (jna/to-fn Integer ssh2/libssh2_session_get_blocking))

; long libssh2_session_get_timeout(LIBSSH2_SESSION* session);
(def get-timeout (jna/to-fn Long ssh2/libssh2_session_get_timeout))

; int libssh2_session_handshake(LIBSSH2_SESSION *session, libssh2_socket_t sock);
(def handshake (jna/to-fn Integer ssh2/libssh2_session_handshake))

; const char *libssh2_session_hostkey(LIBSSH2_SESSION *session,
;                                     size_t *len,
;                                     int *type);
(def hostkey (jna/to-fn Pointer ssh2/libssh2_session_hostkey))

; LIBSSH2_SESSION * libssh2_session_init_ex(LIBSSH2_ALLOC_FUNC((*my_alloc)),
;                                           LIBSSH2_FREE_FUNC((*my_free)),
;                                           LIBSSH2_REALLOC_FUNC((*my_realloc)),
;                                           void *abstract);
(def init-ex (jna/to-fn Pointer ssh2/libssh2_session_init_ex))

; LIBSSH2_SESSION * libssh2_session_init_ex();
(defn init
  []
  (init-ex nil nil nil nil))

; int libssh2_session_last_error(LIBSSH2_SESSION *session,
;                                char **errmsg,
;                                int *errmsg_len,
;                                int want_buf);
(def last-error (jna/to-fn Integer ssh2/libssh2_session_last_error))

; int libssh2_session_last_errno(LIBSSH2_SESSION *session);
(def last-errno (jna/to-fn Integer ssh2/libssh2_session_last_errno))

; int libssh2_session_method_pref(LIBSSH2_SESSION *session,
;                                 int method_type,
;                                 const char *prefs);
(def method-pref (jna/to-fn Integer ssh2/libssh2_session_method_pref))

; const char *libssh2_session_methods(LIBSSH2_SESSION *session, int method_type);
(def methods (jna/to-fn String ssh2/libssh2_session_methods))

; void libssh2_session_set_blocking(LIBSSH2_SESSION* session, int blocking);
(def set-blocking (jna/to-fn Void ssh2/libssh2_session_set_blocking))

; void libssh2_session_set_timeout(LIBSSH2_SESSION* session, long timeout);
(def set-timeout (jna/to-fn Void ssh2/libssh2_session_set_timeout))

; int libssh2_session_startup(LIBSSH2_SESSION *session, int sock);
(def startup (jna/to-fn Integer ssh2/libssh2_session_startup))

; int libssh2_session_supported_algs(LIBSSH2_SESSION* session,
;                                    int method_type,
;                                    const char*** algs);
(def supported-algs (jna/to-fn Integer ssh2/libssh2_session_supported_algs))
