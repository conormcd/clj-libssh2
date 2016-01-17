(ns clj-libssh2.libssh2.keepalive
  (:require [net.n01se.clojure-jna :as jna]))

; void libssh2_keepalive_config (LIBSSH2_SESSION *session,
;                                int want_reply,
;                                unsigned interval);
(def config (jna/to-fn Void ssh2/libssh2_keepalive_config))

; int libssh2_keepalive_send (LIBSSH2_SESSION *session, int *seconds_to_next);
(def send (jna/to-fn Integer ssh2/libssh2_keepalive_send))
