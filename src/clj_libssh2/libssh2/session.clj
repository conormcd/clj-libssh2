(ns clj-libssh2.libssh2.session
  "Create and manipulate libssh2 sessions."
  (:refer-clojure :exclude [methods])
  (:require [net.n01se.clojure-jna :as jna]
            [clj-libssh2.libssh2 :as libssh2])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([session])} abstract
  "void **libssh2_session_abstract(LIBSSH2_SESSION *session);"
  (jna/to-fn Pointer ssh2/libssh2_session_abstract))

(def ^{:arglists '([session])} banner-get
  "const char *libssh2_session_banner_get(LIBSSH2_SESSION *session);"
  (jna/to-fn String ssh2/libssh2_session_banner_get))

(def ^{:arglists '([session banner])} banner-set
  "int libssh2_session_banner_set(LIBSSH2_SESSION *session, const char *banner);"
  (jna/to-fn Integer ssh2/libssh2_session_banner_set))

(def ^{:arglists '([session])} block-directions
  "int libssh2_session_block_directions(LIBSSH2_SESSION *session);"
  (jna/to-fn Integer ssh2/libssh2_session_block_directions))

(def ^{:arglists '([session cbtype callback])} callback-set
  "
   void *libssh2_session_callback_set(LIBSSH2_SESSION *session,
                                      int cbtype,
                                      void *callback);"
  (jna/to-fn Pointer ssh2/libssh2_session_callback_set))

(def ^{:arglists '([session reason description lang])} disconnect-ex
  "
   int libssh2_session_disconnect_ex(LIBSSH2_SESSION *session,
                                     int reason,
                                     const char *description,
                                     const char *lang);"
  (jna/to-fn Integer ssh2/libssh2_session_disconnect_ex))

(defn disconnect
  "
   int libssh2_session_disconnect(LIBSSH2_SESSION *session,
                                  const char *description);"
  [session description]
  (disconnect-ex session libssh2/SSH_DISCONNECT_BY_APPLICATION description ""))

(def ^{:arglists '([session flag value])} flag
  "int libssh2_session_flag(LIBSSH2_SESSION *session, int flag, int value);"
  (jna/to-fn Integer ssh2/libssh2_session_flag))

(def ^{:arglists '([session])} free
  "int libssh2_session_free(LIBSSH2_SESSION *session);"
  (jna/to-fn Integer ssh2/libssh2_session_free))

(def ^{:arglists '([session])} get-blocking
  "int libssh2_session_get_blocking(LIBSSH2_SESSION* session);"
  (jna/to-fn Integer ssh2/libssh2_session_get_blocking))

(def ^{:arglists '([session])} get-timeout
  "long libssh2_session_get_timeout(LIBSSH2_SESSION* session);"
  (jna/to-fn Long ssh2/libssh2_session_get_timeout))

(def ^{:arglists '([session sock])} handshake
  "
   int libssh2_session_handshake(LIBSSH2_SESSION *session,
                                 libssh2_socket_t sock);"
  (jna/to-fn Integer ssh2/libssh2_session_handshake))

(def ^{:arglists '([session len type])} hostkey
  "
   const char *libssh2_session_hostkey(LIBSSH2_SESSION *session,
                                       size_t *len,
                                       int *type);"
  (jna/to-fn Pointer ssh2/libssh2_session_hostkey))

(def ^{:arglists '([my-alloc my-free my-realloc abstract])} init-ex
  "
   LIBSSH2_SESSION * libssh2_session_init_ex(LIBSSH2_ALLOC_FUNC((*my_alloc)),
                                             LIBSSH2_FREE_FUNC((*my_free)),
                                             LIBSSH2_REALLOC_FUNC((*my_realloc)),
                                             void *abstract);"
  (jna/to-fn Pointer ssh2/libssh2_session_init_ex))

(defn init
  "LIBSSH2_SESSION * libssh2_session_init();"
  []
  (init-ex nil nil nil nil))

(def ^{:arglists '([session err-msg err-msg-len want-buf])} last-error
  "
   int libssh2_session_last_error(LIBSSH2_SESSION *session,
                                  char **errmsg,
                                  int *errmsg_len,
                                  int want_buf);"
  (jna/to-fn Integer ssh2/libssh2_session_last_error))

(def ^{:arglists '([session])} last-errno
  "int libssh2_session_last_errno(LIBSSH2_SESSION *session);"
  (jna/to-fn Integer ssh2/libssh2_session_last_errno))

(def ^{:arglists '([session method-type prefs])} method-pref
  "
   int libssh2_session_method_pref(LIBSSH2_SESSION *session,
                                  int method_type,
                                  const char *prefs);"
  (jna/to-fn Integer ssh2/libssh2_session_method_pref))

(def ^{:arglists '([session method-type])} methods
  "
   const char *libssh2_session_methods(LIBSSH2_SESSION *session,
                                       int method_type);"
  (jna/to-fn String ssh2/libssh2_session_methods))

(def ^{:arglists '([session blocking])} set-blocking
  "void libssh2_session_set_blocking(LIBSSH2_SESSION* session, int blocking);"
  (jna/to-fn Void ssh2/libssh2_session_set_blocking))

(def ^{:arglists '([session timeout])} set-timeout
  "void libssh2_session_set_timeout(LIBSSH2_SESSION* session, long timeout);"
  (jna/to-fn Void ssh2/libssh2_session_set_timeout))

(def ^{:arglists '([session sock]) :deprecated "libssh2"} startup
  "int libssh2_session_startup(LIBSSH2_SESSION *session, int sock);"
  (jna/to-fn Integer ssh2/libssh2_session_startup))

(def ^{:arglists '([session method-type algs])} supported-algs
  "
   int libssh2_session_supported_algs(LIBSSH2_SESSION* session,
                                      int method_type,
                                      const char*** algs);"
  (jna/to-fn Integer ssh2/libssh2_session_supported_algs))
