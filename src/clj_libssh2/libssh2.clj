(ns clj-libssh2.libssh2
  (:require [net.n01se.clojure-jna :as jna]))

; All the LIBSSH2_* constants from libssh2.h are here without their LIBSSH2_
; prefix since we have real namespaces in Clojure. Other constants defined in
; libssh2.h are replicated here without alteration.

(def VERSION "1.7.0")
(def VERSION_MAJOR 1)
(def VERSION_MINOR 7)
(def VERSION_PATCH 0)
(def VERSION_NUM 0x010700)
(def SSH_BANNER "SSH-1.0-libssh2_1.7.0")
(def SSH_DEFAULT_BANNER SSH_BANNER)
(def SSH_DEFAULT_BANNER_WITH_CRLF (str SSH_DEFAULT_BANNER "\r\n"))

; Default generate and safe prime sizes for diffie-hellman-group-exchange-sha1
(def DH_GEX_MINGROUP 1024)
(def DH_GEX_OPTGROUP 1536)
(def DH_GEX_MAXGROUP 2048)

; Defaults for pty requests
(def TERM_WIDTH 80)
(def TERM_HEIGHT 24)
(def TERM_WIDTH_PX 0)
(def TERM_HEIGHT_PX 0)

; 1/4 second
(def SOCKET_POLL_UDELAY 250000)
; 0.25 * 120 == 30 seconds
(def SOCKET_POLL_MAXLOOPS 120)

; Maximum size to allow a payload to compress to, plays it safe by falling
; short of spec limits
(def PACKET_MAXCOMP 32000)

; Maximum size to allow a payload to deccompress to, plays it safe by allowing
; more than spec requires
(def PACKET_MAXDECOMP 40000)

; Maximum size for an inbound compressed payload, plays it safe by overshooting
; spec limits
(def PACKET_MAXPAYLOAD 40000)

; libssh2_session_callback_set() constants
(def CALLBACK_IGNORE 0)
(def CALLBACK_DEBUG 1)
(def CALLBACK_DISCONNECT 2)
(def CALLBACK_MACERROR 3)
(def CALLBACK_X11 4)
(def CALLBACK_SEND 5)
(def CALLBACK_RECV 6)

; libssh2_session_method_pref() constants
(def METHOD_KEX 0)
(def METHOD_HOSTKEY 1)
(def METHOD_CRYPT_CS 2)
(def METHOD_CRYPT_SC 3)
(def METHOD_MAC_CS 4)
(def METHOD_MAC_SC 5)
(def METHOD_COMP_CS 6)
(def METHOD_COMP_SC 7)
(def METHOD_LANG_CS 8)
(def METHOD_LANG_SC 9)

; flags
(def FLAG_SIGPIPE 1)
(def FLAG_COMPRESS 2)

; Poll FD Descriptor Types
(def POLLFD_SOCKET 1)
(def POLLFD_CHANNEL 2)
(def POLLFD_LISTENER 3)

(def POLLFD_POLLIN 0x0001) ; Data available to be read or connection available -- All
(def POLLFD_POLLPRI 0x0002) ; Priority data available to be read -- Socket only
(def POLLFD_POLLEXT 0x0002) ; Extended data available to be read -- Channel only
(def POLLFD_POLLOUT 0x0004) ; Can may be written -- Socket/Channel
; revents only
(def POLLFD_POLLERR 0x0008) ; Error Condition -- Socket
(def POLLFD_POLLHUP 0x0010) ; HangUp/EOF -- Socket
(def POLLFD_SESSION_CLOSED 0x0010) ; Session Disconnect
(def POLLFD_POLLNVAL 0x0020) ; Invalid request -- Socket Only
(def POLLFD_POLLEX 0x0040) ; Exception Condition -- Socket/Win32
(def POLLFD_CHANNEL_CLOSED 0x0080) ; Channel Disconnect
(def POLLFD_LISTENER_CLOSED 0x0080) ; Listener Disconnect

; Block Direction Types
(def SESSION_BLOCK_INBOUND 0x0001)
(def SESSION_BLOCK_OUTBOUND 0x0002)

; Hash Types
(def HOSTKEY_HASH_MD5 1)
(def HOSTKEY_HASH_SHA1 2)

; Hostkey Types
(def HOSTKEY_TYPE_UNKNOWN 0)
(def HOSTKEY_TYPE_RSA 1)
(def HOSTKEY_TYPE_DSS 2)

; Disconnect Codes (defined by SSH protocol)
(def SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT 1)
(def SSH_DISCONNECT_PROTOCOL_ERROR 2)
(def SSH_DISCONNECT_KEY_EXCHANGE_FAILED 3)
(def SSH_DISCONNECT_RESERVED 4)
(def SSH_DISCONNECT_MAC_ERROR 5)
(def SSH_DISCONNECT_COMPRESSION_ERROR 6)
(def SSH_DISCONNECT_SERVICE_NOT_AVAILABLE 7)
(def SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED 8)
(def SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE 9)
(def SSH_DISCONNECT_CONNECTION_LOST 10)
(def SSH_DISCONNECT_BY_APPLICATION 11)
(def SSH_DISCONNECT_TOO_MANY_CONNECTIONS 12)
(def SSH_DISCONNECT_AUTH_CANCELLED_BY_USER 13)
(def SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE 14)
(def SSH_DISCONNECT_ILLEGAL_USER_NAME 15)
(def SSH_EXTENDED_DATA_STDERR 1)

; Error Codes (defined by libssh2)
(def ERROR_NONE 0)
(def ERROR_SOCKET_NONE -1)
(def ERROR_BANNER_RECV -2)
(def ERROR_BANNER_SEND -3)
(def ERROR_INVALID_MAC -4)
(def ERROR_KEX_FAILURE -5)
(def ERROR_ALLOC -6)
(def ERROR_SOCKET_SEND -7)
(def ERROR_KEY_EXCHANGE_FAILURE -8)
(def ERROR_TIMEOUT -9)
(def ERROR_HOSTKEY_INIT -10)
(def ERROR_HOSTKEY_SIGN -11)
(def ERROR_DECRYPT -12)
(def ERROR_SOCKET_DISCONNECT -13)
(def ERROR_PROTO -14)
(def ERROR_PASSWORD_EXPIRED -15)
(def ERROR_FILE -16)
(def ERROR_METHOD_NONE -17)
(def ERROR_AUTHENTICATION_FAILED -18)
(def ERROR_PUBLICKEY_UNRECOGNIZED ERROR_AUTHENTICATION_FAILED)
(def ERROR_PUBLICKEY_UNVERIFIED -19)
(def ERROR_CHANNEL_OUTOFORDER -20)
(def ERROR_CHANNEL_FAILURE -21)
(def ERROR_CHANNEL_REQUEST_DENIED -22)
(def ERROR_CHANNEL_UNKNOWN -23)
(def ERROR_CHANNEL_WINDOW_EXCEEDED -24)
(def ERROR_CHANNEL_PACKET_EXCEEDED -25)
(def ERROR_CHANNEL_CLOSED -26)
(def ERROR_CHANNEL_EOF_SENT -27)
(def ERROR_SCP_PROTOCOL -28)
(def ERROR_ZLIB -29)
(def ERROR_SOCKET_TIMEOUT -30)
(def ERROR_SFTP_PROTOCOL -31)
(def ERROR_REQUEST_DENIED -32)
(def ERROR_METHOD_NOT_SUPPORTED -33)
(def ERROR_INVAL -34)
(def ERROR_INVALID_POLL_TYPE -35)
(def ERROR_PUBLICKEY_PROTOCOL -36)
(def ERROR_EAGAIN -37)
(def ERROR_BUFFER_TOO_SMALL -38)
(def ERROR_BAD_USE -39)
(def ERROR_COMPRESS -40)
(def ERROR_OUT_OF_BOUNDARY -41)
(def ERROR_AGENT_PROTOCOL -42)
(def ERROR_SOCKET_RECV -43)
(def ERROR_ENCRYPT -44)
(def ERROR_BAD_SOCKET -45)
(def ERROR_KNOWN_HOSTS -46)

; this is a define to provide the old (<= 1.2.7) name
(def ERROR_BANNER_NONE ERROR_BANNER_RECV)

; Global API
(def INIT_NO_CRYPTO 0x0001)

; Channel API
(def CHANNEL_WINDOW_DEFAULT (* 2 1024 1024))
(def CHANNEL_PACKET_DEFAULT 32768)
(def CHANNEL_MINADJUST 1024)
(def CHANNEL_EXTENDED_DATA_NORMAL 0)
(def CHANNEL_EXTENDED_DATA_IGNORE 1)
(def CHANNEL_EXTENDED_DATA_MERGE 2)
(def SSH_EXTENDED_DATA_STDERR 1)
(def CHANNEL_FLUSH_EXTENDED_DATA -1)
(def CHANNEL_FLUSH_ALL -2)

; host format (2 bits)
(def KNOWNHOST_TYPE_MASK 0xffff)
(def KNOWNHOST_TYPE_PLAIN 1)
(def KNOWNHOST_TYPE_SHA1 2) ; always base64 encoded
(def KNOWNHOST_TYPE_CUSTOM 3)

; key format (2 bits)
(def KNOWNHOST_KEYENC_MASK (bit-shift-left 3 16))
(def KNOWNHOST_KEYENC_RAW (bit-shift-left 1 16))
(def KNOWNHOST_KEYENC_BASE64 (bit-shift-left 2 16))

; type of key (2 bits)
(def KNOWNHOST_KEY_MASK (bit-shift-left 7 18))
(def KNOWNHOST_KEY_SHIFT 18)
(def KNOWNHOST_KEY_RSA1 (bit-shift-left 1 18))
(def KNOWNHOST_KEY_SSHRSA (bit-shift-left 2 18))
(def KNOWNHOST_KEY_SSHDSS (bit-shift-left 3 18))
(def KNOWNHOST_KEY_UNKNOWN (bit-shift-left 7 18))

(def KNOWNHOST_CHECK_MATCH 0)
(def KNOWNHOST_CHECK_MISMATCH 1)
(def KNOWNHOST_CHECK_NOTFOUND 2)
(def KNOWNHOST_CHECK_FAILURE 3)

(def KNOWNHOST_FILE_OPENSSH 1)

; These are included for completeness. The trace API won't work because the
; bundled libraries aren't built with debug enabled.
(def TRACE_TRANS (bit-shift-left 1 1))
(def TRACE_KEX (bit-shift-left 1 2))
(def TRACE_AUTH (bit-shift-left 1 3))
(def TRACE_CONN (bit-shift-left 1 4))
(def TRACE_SCP (bit-shift-left 1 5))
(def TRACE_SFTP (bit-shift-left 1 6))
(def TRACE_ERROR (bit-shift-left 1 7))
(def TRACE_PUBLICKEY (bit-shift-left 1 8))
(def TRACE_SOCKET (bit-shift-left 1 9))

(def ^{:arglists '([session banner]) :deprecated "libssh2"} banner-set
  "int libssh2_banner_set(LIBSSH2_SESSION *session, const char *banner);"
  (jna/to-fn Integer ssh2/libssh2_banner_set))

(def ^{:arglists '([session dest dest-len src src-len]) :deprecated "libssh2"} base64-decode
  "
   int libssh2_base64_decode(LIBSSH2_SESSION *session,
                             char **dest
                             unsigned int *dest_len
                             const char *src
                             unsigned int src_len);"
  (jna/to-fn Integer ssh2/libssh2_base64_decode))

(def ^{:arglists '([])} exit
  "void libssh2_exit(void);"
  (jna/to-fn Void ssh2/libssh2_exit))

(def ^{:arglists '([session ptr])} free
  "void libssh2_free(LIBSSH2_SESSION *session, void *ptr);"
  (jna/to-fn Void ssh2/libssh2_free))

(def ^{:arglists '([session hash-type])} hostkey-hash
  "const char * libssh2_hostkey_hash(LIBSSH2_SESSION *session, int hash_type);"
  (jna/to-fn String ssh2/libssh2_hostkey_hash))

(def ^{:arglists '([flags])} init
  "int libssh2_init(int flags);"
  (jna/to-fn Integer ssh2/libssh2_init))

(def ^{:arglists '([fds nfds timeout]) :deprecated "libssh2"} poll
  "int libssh2_poll(LIBSSH2_POLLFD *fds, unsigned int nfds, long timeout);"
  (jna/to-fn Integer ssh2/libssh2_poll))

(def ^{:arglists '([channel extended]) :deprecated "libssh2"} poll-channel-read
  "int libssh2_poll_channel_read(LIBSSH2_CHANNEL *channel, int extended);"
  (jna/to-fn Integer ssh2/libssh2_poll_channel_read))

(def ^{:arglists '([session bitmask])} trace
  "int libssh2_trace(LIBSSH2_SESSION *session, int bitmask);

   Note: This will not work because the bundled copy of libssh2 is a release
   build and tracing is disabled in release builds."
  (jna/to-fn Integer ssh2/libssh2_trace))

(def ^{:arglists '([session context callback])} trace-sethandler
  "
   int libssh2_trace_sethandler(LIBSSH2_SESSION *session,
                                void* context,
                                libssh2_trace_handler_func callback);

   Note: This will not work because the bundled copy of libssh2 is a release
   build and tracing is disabled in release builds."
  (jna/to-fn Integer ssh2/libssh2_trace_sethandler))

(def ^{:arglists '([required-version])} version
  "const char * libssh2_version(int required_version);"
  (jna/to-fn String ssh2/libssh2_version))
