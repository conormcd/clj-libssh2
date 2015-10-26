(ns clj-libssh2.libssh2.agent
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

; int libssh2_agent_connect(LIBSSH2_AGENT *agent);
(def connect (jna/to-fn Integer ssh2/libssh2_agent_connect))

; int libssh2_agent_disconnect (LIBSSH2_AGENT *agent);
(def disconnect (jna/to-fn Integer ssh2/libssh2_agent_disconnect))

; void libssh2_agent_free(LIBSSH2_AGENT *agent);
(def free (jna/to-fn Void ssh2/libssh2_agent_free))

; int libssh2_agent_get_identity(LIBSSH2_AGENT *agent,
;                                 struct libssh2_agent_publickey **store,
;                                 struct libssh2_agent_publickey *prev);
(def get-identity (jna/to-fn Integer ssh2/libssh2_agent_get_identity))

; LIBSSH2_AGENT *libssh2_agent_init(LIBSSH2_SESSION *session);
(def init (jna/to-fn Pointer ssh2/libssh2_agent_init))

; int libssh2_agent_list_identities(LIBSSH2_AGENT *agent);
(def list-identities (jna/to-fn Integer ssh2/libssh2_agent_list_identities))

; int libssh2_agent_userauth(LIBSSH2_AGENT *agent,
;                             const char *username,
;                             struct libssh2_agent_publickey *identity);
(def userauth (jna/to-fn Integer ssh2/libssh2_agent_userauth))
