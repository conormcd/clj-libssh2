(ns clj-libssh2.libssh2.userauth
  "Functions for authenticating a user."
  (:refer-clojure :exclude [list])
  (:require [net.n01se.clojure-jna :as jna]))

(def ^{:arglists '([session])} authenticated
  "int libssh2_userauth_authenticated(LIBSSH2_SESSION *session);"
  (jna/to-fn String ssh2/libssh2_userauth_authenticated))

(def ^{:arglists '([session username username-len publickey privatekey passphrase hostname hostname-len local-username local-username-len])} hostbased-fromfile-ex
  "
   int libssh2_userauth_hostbased_fromfile_ex(LIBSSH2_SESSION *session,
                                              const char *username,
                                              unsigned int username_len,
                                              const char *publickey,
                                              const char *privatekey,
                                              const char *passphrase,
                                              const char *hostname,
                                              unsigned int hostname_len,
                                              const char *local_username,
                                              unsigned int *local_username_len);"
  (jna/to-fn Integer ssh2/libssh2_userauth_hostbased_fromfile_ex))

(defn ^{:arglists '([session username publickey privatekey passphrase hostname])} hostbased-fromfile
  "
   int libssh2_userauth_hostbased_fromfile(LIBSSH2_SESSION *session,
                                           const char *username,
                                           const char *publickey,
                                           const char *privatekey,
                                           const char *passphrase,
                                           const char *hostname);"
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

(def ^{:arglists '([session username username-len response-callback])} keyboard-interactive-ex
  "
   int libssh2_userauth_keyboard_interactive_ex(LIBSSH2_SESSION *session,
                                                const char *username,
                                                unsigned int username_len,
                                                LIBSSH2_USERAUTH_KBDINT_RESPONSE_FUNC(*response_callback));"
  (jna/to-fn Integer ssh2/libssh2_userauth_keyboard_interactive_ex))

(defn keyboard-interactive
  "
   int libssh2_userauth_keyboard_interactive(LIBSSH2_SESSION* session,
                                             const char *username,
                                             LIBSSH2_USERAUTH_KBDINT_RESPONSE_FUNC((*response_callback)));"
  [session username response-callback]
  (keyboard-interactive-ex session username (count username) response-callback))

(def ^{:arglists '([session username username-len])} list
  "
   char * libssh2_userauth_list(LIBSSH2_SESSION *session,
                                const char *username,
                                unsigned int username_len);"
  (jna/to-fn String ssh2/libssh2_userauth_list))

(def ^{:arglists '([session username username-len password password-len passwd_change_cb])} password-ex
  "
   int libssh2_userauth_password_ex(LIBSSH2_SESSION *session,
                                    const char *username,
                                    unsigned int username_len,
                                    const char *password,
                                    unsigned int password_len,
                                    LIBSSH2_PASSWD_CHANGEREQ_FUNC((*passwd_change_cb)));"
  (jna/to-fn Integer ssh2/libssh2_userauth_password_ex))

(defn password
  "
   int libssh2_userauth_password(LIBSSH2_SESSION *session,
                                 const char* username,
                                 const char* password);"
  [session username password]
  (password-ex session
               username
               (count username)
               password
               (count password)
               nil))


(def ^{:arglists '([session user pubkeydata pubkeydata-len sign-callback abstract])} publickey
  "
   int libssh2_userauth_publickey(LIBSSH2_SESSION *session,
                                  const char *user,
                                  const unsigned char *pubkeydata,
                                  size_t pubkeydata_len,
                                  sign_callback,
                                  void **abstract);"
  (jna/to-fn Integer ssh2/libssh2_userauth_publickey))

(def ^{:arglists '([session username username-len publickey privatekey passphrase])} publickey-fromfile-ex
  "
   int libssh2_userauth_publickey_fromfile_ex(LIBSSH2_SESSION *session,
                                              const char *username,
                                              unsigned int ousername_len,
                                              const char *publickey,
                                              const char *privatekey,
                                              const char *passphrase);"
  (jna/to-fn Integer ssh2/libssh2_userauth_publickey_fromfile_ex))

(defn publickey-fromfile
  "
   int libssh2_userauth_publickey_fromfile(LIBSSH2_SESSION *session,
                                           const char *username,
                                           const char *publickey,
                                           const char *privatekey,
                                           const char *passphrase);"
  [session username publickey privatekey passphrase]
  (publickey-fromfile-ex session
                         username
                         (count username)
                         publickey
                         privatekey
                         passphrase))

(def ^{:arglists '([session username username-len publickeydata publickeydata-len privatekeydata privatekeydata-len passphrase])} publickey-frommemory
  "
   int libssh2_userauth_publickey_frommemory(LIBSSH2_SESSION *session,
                                             const char *username,
                                             size_t username_len,
                                             const char *publickeydata,
                                             size_t publickeydata_len,
                                             const char *privatekeydata,
                                             size_t privatekeydata_len,
                                             const char *passphrase);"
  (jna/to-fn Integer ssh2/libssh2_userauth_publickey_frommemory))
