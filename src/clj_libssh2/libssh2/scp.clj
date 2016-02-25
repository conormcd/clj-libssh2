(ns clj-libssh2.libssh2.scp
  "Transfer files using SCP"
  (:refer-clojure :exclude [send])
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([session path sb]) :deprecated "libssh2"} recv
  "
   LIBSSH2_CHANNEL *libssh2_scp_recv(LIBSSH2_SESSION *session,
                                     const char *path,
                                     struct stat *sb);"
  (jna/to-fn Pointer ssh2/libssh2_scp_recv))

(def ^{:arglists '([session path sb])} recv2
  "
   LIBSSH2_CHANNEL *libssh2_scp_recv(LIBSSH2_SESSION *session,
                                     const char *path,
                                     libssh2_struct_stat *sb);"
  (jna/to-fn Pointer ssh2/libssh2_scp_recv2))

(def ^{:arglists '([session path mode size mtime atime]) :deprecated "libssh2"} send-ex
  "
   LIBSSH2_CHANNEL *libssh2_scp_send_ex(LIBSSH2_SESSION *session,
                                        const char *path,
                                        int mode,
                                        size_t size,
                                        long mtime,
                                        long atime);"
  (jna/to-fn Pointer ssh2/libssh2_scp_send_ex))

(def ^{:arglists '([session path mode size mtime atime])} send64
  "
   LIBSSH2_CHANNEL * libssh2_scp_send64 (LIBSSH2_SESSION *session,
                                         const char *path,
                                         int mode,
                                         libssh2_int64_t size,
                                         time_t mtime,
                                         time_t atime);"
  (jna/to-fn Pointer ssh2/libssh2_scp_send64))

(defn ^{:deprecated "libssh2"} send
  "
   LIBSSH2_CHANNEL * libssh2_scp_send (LIBSSH2_SESSION *session,
                                       const char *path,
                                       int mode,
                                       libssh2_int64_t size);"
  [session path mode size]
  (send-ex session path mode size 0 0))
