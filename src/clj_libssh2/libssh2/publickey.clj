(ns clj-libssh2.libssh2.publickey
  "Functions for using the publickey subsystem. (RFC 4819)"
  (:refer-clojure :exclude [remove])
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([pkey name name-len blob blob-len overwrite num-attrs attrs])} add-ex
  "
   int libssh2_publickey_add_ex(LIBSSH2_PUBLICKEY *pkey,
                                const unsigned char *name,
                                unsigned long name_len,
                                const unsigned char *blob,
                                unsigned long blob_len,
                                char overwrite,
                                unsigned long num_attrs,
                                const libssh2_publickey_attribute attrs[]);"
  (jna/to-fn Integer ssh2/libssh2_publickey_add_ex))

(defn add
  "
   int libssh2_publickey_add(LIBSSH2_PUBLICKEY *pkey,
                             const unsigned char *name,
                             const unsigned char *blob,
                             unsigned long blob_len,
                             char overwrite,
                             unsigned long num_attrs,
                             const libssh2_publickey_attribute attrs[]);"
  [pkey name blob blob-len overwrite num-attrs attrs]
  (add-ex pkey name (count name) blob blob-len overwrite num-attrs attrs))

(def ^{:arglists '([session])} init
  "LIBSSH2_PUBLICKEY *libssh2_publickey_init(LIBSSH2_SESSION *session);"
  (jna/to-fn Pointer ssh2/libssh2_publickey_init))

(def ^{:arglists '([pkey num-keys pkey-list])} list-fetch
  "
   int libssh2_publickey_list_fetch(LIBSSH2_PUBLICKEY *pkey,
                                    unsigned long *num_keys,
                                    libssh2_publickey_list **pkey_list);"
  (jna/to-fn Integer ssh2/libssh2_publickey_list_fetch))

(def ^{:arglists '([pkey pkey-list])} list-free
  "
   void libssh2_publickey_list_free(LIBSSH2_PUBLICKEY *pkey,
                                    libssh2_publickey_list *pkey_list);"
  (jna/to-fn Void ssh2/libssh2_publickey_list_free))

(def ^{:arglists '([pkey name name-len blob blob-len])} remove-ex
  "
   int libssh2_publickey_remove_ex(LIBSSH2_PUBLICKEY *pkey,
                                   const unsigned char *name,
                                   unsigned long name_len,
                                   const unsigned char *blob,
                                   unsigned long blob_len);"
  (jna/to-fn Integer ssh2/libssh2_publickey_remove_ex))

(defn remove
  "
   int libssh2_publickey_remove(LIBSSH2_PUBLICKEY *pkey,
                                const unsigned char *name,
                                const unsigned char *blob,
                                unsigned long blob_len);"
  [pkey name blob blob-len]
  (remove-ex pkey name (count name) blob blob-len))

(def ^{:arglists '([pkey])} shutdown
  "int libssh2_publickey_shutdown(LIBSSH2_PUBLICKEY *pkey);"
  (jna/to-fn Integer ssh2/libssh2_publickey_shutdown))
