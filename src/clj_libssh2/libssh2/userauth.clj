(ns clj-libssh2.libssh2.userauth
  (:require [net.n01se.clojure-jna :as jna]))

; int libssh2_userauth_authenticated(LIBSSH2_SESSION *session);
(def authenticated (jna/to-fn String ssh2/libssh2_userauth_authenticated))

; int libssh2_userauth_hostbased_fromfile_ex(LIBSSH2_SESSION *session,
;                                            const char *username,
;                                            unsigned int username_len,
;                                            const char *publickey,
;                                            const char *privatekey,
;                                            const char *passphrase,
;                                            const char *hostname,
;                                            unsigned int hostname_len,
;                                            const char *local_username,
;                                            unsigned int *local_username_len);
(def hostbased-fromfile-ex (jna/to-fn Integer ssh2/libssh2_userauth_hostbased_fromfile_ex))

; int libssh2_userauth_hostbased_fromfile(LIBSSH2_SESSION *session,
;                                         const char *username,
;                                         const char *publickey,
;                                         const char *privatekey,
;                                         const char *passphrase,
;                                         const char *hostname);
(defn hostbased-fromfile
  [session username publickey privatekey passphrase hostname]
  (hostbased-fromfile-ex session
                         username
                         (count username)
                         publickey
                         privatekey
                         passphrase
                         hostname
                         (count hostname)
                         username
                         (count username)))

; int libssh2_userauth_keyboard_interactive_ex(LIBSSH2_SESSION *session,
;                                              const char *username,
;                                              unsigned int username_len,
;                                              LIBSSH2_USERAUTH_KBDINT_RESPONSE_FUNC(*response_callback));
(def keyboard-interactive-ex (jna/to-fn Integer ssh2/libssh2_userauth_keyboard_interactive_ex))

; int libssh2_userauth_keyboard_interactive(LIBSSH2_SESSION* session,
;                                           const char *username,
;                                           LIBSSH2_USERAUTH_KBDINT_RESPONSE_FUNC((*response_callback)));
(defn keyboard-interactive
  [session username response-callback]
  (keyboard-interactive-ex session username (count username) response-callback))

; char * libssh2_userauth_list(LIBSSH2_SESSION *session,
;                              const char *username,
;                              unsigned int username_len);
(def list (jna/to-fn String ssh2/libssh2_userauth_list))

; int libssh2_userauth_password_ex(LIBSSH2_SESSION *session,
;                                  const char *username,
;                                  unsigned int username_len,
;                                  const char *password,
;                                  unsigned int password_len,
;                                  LIBSSH2_PASSWD_CHANGEREQ_FUNC((*passwd_change_cb)));
(def password-ex (jna/to-fn Integer ssh2/libssh2_userauth_password_ex))

; libssh2_userauth_password(LIBSSH2_SESSION *session,
;                           const char* username,
;                           const char* password);
(defn password
  [session username password]
  (password-ex session
               username
               (count username)
               password
               (count password)
               nil))


; int libssh2_userauth_publickey(LIBSSH2_SESSION *session,
;                                const char *user,
;                                const unsigned char *pubkeydata,
;                                size_t pubkeydata_len,
;                                sign_callback,
;                                void **abstract);
(def publickey (jna/to-fn Integer ssh2/libssh2_userauth_publickey))

; int libssh2_userauth_publickey_fromfile_ex(LIBSSH2_SESSION *session,
;                                            const char *username,
;                                            unsigned int ousername_len,
;                                            const char *publickey,
;                                            const char *privatekey,
;                                            const char *passphrase);
(def publickey-fromfile-ex (jna/to-fn Integer ssh2/libssh2_userauth_publickey_fromfile_ex))

; libssh2_userauth_publickey_fromfile(LIBSSH2_SESSION *session,
;                                     const char *username,
;                                     const char *publickey,
;                                     const char *privatekey,
;                                     const char *passphrase);
(defn publickey-fromfile
  [session username publickey privatekey passphrase]
  (publickey-fromfile-ex session
                         username
                         (count username)
                         publickey
                         privatekey
                         passphrase))

; int libssh2_userauth_publickey_frommemory(LIBSSH2_SESSION *session,
;                                            const char *username,
;                                            size_t username_len,
;                                            const char *publickeydata,
;                                            size_t publickeydata_len,
;                                            const char *privatekeydata,
;                                            size_t privatekeydata_len,
;                                            const char *passphrase);
(def publickey-frommemory (jna/to-fn Integer ssh2/libssh2_userauth_publickey_frommemory))
