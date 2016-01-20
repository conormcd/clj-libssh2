(ns clj-libssh2.libssh2.knownhost
  "Functions for checking a remote host's fingerprint against a known hosts
   file."
  (:refer-clojure :exclude [get])
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([hosts host salt key keylen typemark store]) :deprecated "libssh2"} add
  "
   int libssh2_knownhost_add(LIBSSH2_KNOWNHOSTS *hosts,
                             const char *host,
                             const char *salt,
                             const char *key,
                             size_t keylen,
                             int typemask,
                             struct libssh2_knownhost **store);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_add))

(def ^{:arglists '([hosts host salt key keylen comment commentlen typemask store])} addc
  "
   int libssh2_knownhost_addc(LIBSSH2_KNOWNHOSTS *hosts,
                              const char *host,
                              const char *salt,
                              const char *key,
                              size_t keylen,
                              const char *comment,
                              size_t commentlen,
                              int typemask,
                              struct libssh2_knownhost **store);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_addc))

(def ^{:arglists '([hosts host key keylen typemask knownhost])} check
  "
   int libssh2_knownhost_check(LIBSSH2_KNOWNHOSTS *hosts,
                               const char *host,
                               const char *key,
                               size_t keylen,
                               int typemask,
                               struct libssh2_knownhost **knownhost);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_check))

(def ^{:arglists '([hosts host port key keylen typemask knownhost])} checkp
  "
   int libssh2_knownhost_checkp(LIBSSH2_KNOWNHOSTS *hosts,
                                const char *host,
                                int port,
                                const char *key,
                                size_t keylen,
                                int typemask,
                                struct libssh2_knownhost **knownhost);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_checkp))

(def ^{:arglists '([hosts entry])} del
  "
   int libssh2_knownhost_del(LIBSSH2_KNOWNHOSTS *hosts,
                             struct libssh2_knownhost *entry);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_del))

(def ^{:arglists '([hosts])} free
  "void libssh2_knownhost_free(LIBSSH2_KNOWNHOSTS *hosts);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_free))

(def ^{:arglists '([hosts store prev])} get
  "
   int libssh2_knownhost_get(LIBSSH2_KNOWNHOSTS *hosts,
                             struct libssh2_knownhost **store,
                             struct libssh2_knownhost *prev);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_get))

(def ^{:arglists '([session])} init
  "LIBSSH2_KNOWNHOSTS * libssh2_knownhost_init(LIBSSH2_SESSION *session);"
  (jna/to-fn Pointer ssh2/libssh2_knownhost_init))

(def ^{:arglists '([hosts filename type])} readfile
  "
   int libssh2_knownhost_readfile(LIBSSH2_KNOWNHOSTS *hosts,
                                  const char *filename,
                                  int type);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_readfile))

(def ^{:arglists '([hosts line len type])} readline
  "
   int libssh2_knownhost_readline(LIBSSH2_KNOWNHOSTS *hosts,
                                  const char *line,
                                  size_t len,
                                  int type);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_readline))

(def ^{:arglists '([hosts filename type])} writefile
  "
   int libssh2_knownhost_writefile(LIBSSH2_KNOWNHOSTS *hosts,
                                   const char *filename,
                                   int type);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_writefile))

(def ^{:arglists '([hosts known buffer buflen outlen type])} writeline
  "
   int libssh2_knownhost_writeline(LIBSSH2_KNOWNHOSTS *hosts,
                                   struct libssh2_knownhost *known,
                                   char *buffer,
                                   size_t buflen,
                                   size_t *outlen,
                                   int type);"
  (jna/to-fn Integer ssh2/libssh2_knownhost_writeline))
