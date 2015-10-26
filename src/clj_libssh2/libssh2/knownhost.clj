(ns clj-libssh2.libssh2.knownhost
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

; int libssh2_knownhost_add(LIBSSH2_KNOWNHOSTS *hosts,
;                           const char *host,
;                           const char *salt,
;                           const char *key,
;                           size_t keylen,
;                           int typemask,
;                           struct libssh2_knownhost **store);
(def add (jna/to-fn Integer ssh2/libssh2_knownhost_add))

; int libssh2_knownhost_addc(LIBSSH2_KNOWNHOSTS *hosts,
;                            const char *host,
;                            const char *salt,
;                            const char *key,
;                            size_t keylen,
;                            const char *comment,
;                            size_t commentlen,
;                            int typemask,
;                            struct libssh2_knownhost **store);
(def addc (jna/to-fn Integer ssh2/libssh2_knownhost_addc))

; int libssh2_knownhost_check(LIBSSH2_KNOWNHOSTS *hosts,
;                             const char *host,
;                             const char *key,
;                             size_t keylen,
;                             int typemask,
;                             struct libssh2_knownhost **knownhost);
(def check (jna/to-fn Integer ssh2/libssh2_knownhost_check))

; int libssh2_knownhost_checkp(LIBSSH2_KNOWNHOSTS *hosts,
;                              const char *host,
;                              int port,
;                              const char *key,
;                              size_t keylen,
;                              int typemask,
;                              struct libssh2_knownhost **knownhost);
(def checkp (jna/to-fn Integer ssh2/libssh2_knownhost_checkp))

; int libssh2_knownhost_del(LIBSSH2_KNOWNHOSTS *hosts,
;                            struct libssh2_knownhost *entry);
(def del (jna/to-fn Integer ssh2/libssh2_knownhost_del))

; void libssh2_knownhost_free(LIBSSH2_KNOWNHOSTS *hosts);
(def free (jna/to-fn Integer ssh2/libssh2_knownhost_free))

; int libssh2_knownhost_get(LIBSSH2_KNOWNHOSTS *hosts,
;                           struct libssh2_knownhost **store,
;                           struct libssh2_knownhost *prev);
(def get (jna/to-fn Integer ssh2/libssh2_knownhost_get))

; LIBSSH2_KNOWNHOSTS * libssh2_knownhost_init(LIBSSH2_SESSION *session);
(def init (jna/to-fn Pointer ssh2/libssh2_knownhost_init))

; int libssh2_knownhost_readfile(LIBSSH2_KNOWNHOSTS *hosts,
;                                const char *filename,
;                                int type);
(def readfile (jna/to-fn Integer ssh2/libssh2_knownhost_readfile))

; int libssh2_knownhost_readline(LIBSSH2_KNOWNHOSTS *hosts,
;                                const char *line,
;                                size_t len,
;                                int type);
(def readline (jna/to-fn Integer ssh2/libssh2_knownhost_readline))

; int libssh2_knownhost_writefile(LIBSSH2_KNOWNHOSTS *hosts,
;                                 const char *filename,
;                                 int type);
(def writefile (jna/to-fn Integer ssh2/libssh2_knownhost_writefile))

; int libssh2_knownhost_writeline(LIBSSH2_KNOWNHOSTS *hosts,
;                                 struct libssh2_knownhost *known,
;                                 char *buffer,
;                                 size_t buflen,
;                                 size_t *outlen,
;                                 int type);
(def writeline (jna/to-fn Integer ssh2/libssh2_knownhost_writeline))
