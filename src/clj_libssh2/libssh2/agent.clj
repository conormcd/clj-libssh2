(ns clj-libssh2.libssh2.agent
  "JNA functions for calling functions in libssh2 with names libssh2_agent_*"
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

(def ^{:arglists '([agent])} connect
  "int libssh2_agent_connect(LIBSSH2_AGENT *agent);"
  (jna/to-fn Integer ssh2/libssh2_agent_connect))

(def ^{:arglists '([agent])} disconnect
  "int libssh2_agent_disconnect (LIBSSH2_AGENT *agent);"
  (jna/to-fn Integer ssh2/libssh2_agent_disconnect))

(def ^{:arglists '([agent])} free
  "void libssh2_agent_free(LIBSSH2_AGENT *agent);"
  (jna/to-fn Void ssh2/libssh2_agent_free))

(def ^{:arglists '([agent store prev])} get-identity
  "
   int libssh2_agent_get_identity(LIBSSH2_AGENT *agent,
                                  struct libssh2_agent_publickey **store,
                                  struct libssh2_agent_publickey *prev);"
  (jna/to-fn Integer ssh2/libssh2_agent_get_identity))

(def ^{:arglists '([session])} init
  "LIBSSH2_AGENT *libssh2_agent_init(LIBSSH2_SESSION *session);"
  (jna/to-fn Pointer ssh2/libssh2_agent_init))

(def ^{:arglists '([agent])} list-identities
  "int libssh2_agent_list_identities(LIBSSH2_AGENT *agent);"
  (jna/to-fn Integer ssh2/libssh2_agent_list_identities))

(def ^{:arglists '([agent username identity])} userauth
  "
   int libssh2_agent_userauth(LIBSSH2_AGENT *agent,
                              const char *username,
                              struct libssh2_agent_publickey *identity);"
  (jna/to-fn Integer ssh2/libssh2_agent_userauth))
