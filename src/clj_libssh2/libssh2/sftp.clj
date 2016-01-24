(ns clj-libssh2.libssh2.sftp
  "Functions for using the SFTP subsystem"
  (:refer-clojure :exclude [read])
  (:require [net.n01se.clojure-jna :as jna])
  (:import [com.sun.jna Pointer]))

; Flags for open-ex
(def OPENFILE 0)
(def OPENDIR 1)

; Flags for rename-ex
(def RENAME_OVERWRITE 0x00000001)
(def RENAME_ATOMIC 0x00000002)
(def RENAME_NATIVE 0x00000004)

; Flags for stat-ex
(def STAT 0)
(def LSTAT 1)
(def SETSTAT 2)

; Flags for symlink-ex
(def SYMLINK 0)
(def READLINK 1)
(def REALPATH 2)

; SFTP attribute flag bits
(def ATTR_SIZE 0x00000001)
(def ATTR_UIDGID 0x00000002)
(def ATTR_PERMISSIONS 0x00000004)
(def ATTR_ACMODTIME 0x00000008)
(def ATTR_EXTENDED 0x80000000)

; SFTP statvfs flag bits
(def ST_RDONLY 0x00000001)
(def ST_NOSUID 0x00000002)

; SFTP filetypes
(def TYPE_REGULAR 1)
(def TYPE_DIRECTORY 2)
(def TYPE_SYMLINK 3)
(def TYPE_SPECIAL 4)
(def TYPE_UNKNOWN 5)
(def TYPE_SOCKET 6)
(def TYPE_CHAR_DEVICE 7)
(def TYPE_BLOCK_DEVICE 8)
(def TYPE_FIFO 9)

; File type
(def S_IFMT 0170000) ; type of file mask
(def S_IFIFO 0010000) ; named pipe (fifo)
(def S_IFCHR 0020000) ; character special
(def S_IFDIR 0040000) ; directory
(def S_IFBLK 0060000) ; block special
(def S_IFREG 0100000) ; regular
(def S_IFLNK 0120000) ; symbolic link
(def S_IFSOCK 0140000) ; socket

; File mode
; Read, write, execute/search by owner
(def S_IRWXU 0000700) ; RWX mask for owner
(def S_IRUSR 0000400) ; R for owner
(def S_IWUSR 0000200) ; W for owner
(def S_IXUSR 0000100) ; X for owner
; Read, write, execute/search by group
(def S_IRWXG 0000070) ; RWX mask for group
(def S_IRGRP 0000040) ; R for group
(def S_IWGRP 0000020) ; W for group
(def S_IXGRP 0000010) ; X for group
; Read, write, execute/search by others
(def S_IRWXO 0000007) ; RWX mask for other
(def S_IROTH 0000004) ; R for other
(def S_IWOTH 0000002) ; W for other
(def S_IXOTH 0000001) ; X for other

; Checks for specific file types
(defn S_ISLNK [m] (= S_IFLNK (bit-and m S_IFMT)))
(defn S_ISREG [m] (= S_IFREG (bit-and m S_IFMT)))
(defn S_ISDIR [m] (= S_IFDIR (bit-and m S_IFMT)))
(defn S_ISCHR [m] (= S_IFCHR (bit-and m S_IFMT)))
(defn S_ISBLK [m] (= S_IFBLK (bit-and m S_IFMT)))
(defn S_ISFIFO [m] (= S_IFIFO (bit-and m S_IFMT)))
(defn S_ISSOCK [m] (= S_IFSOCK (bit-and m S_IFMT)))

; SFTP File Transfer Flags -- (e.g. flags parameter to sftp_open())
; Danger will robinson... APPEND doesn't have any effect on OpenSSH servers
(def FXF_READ 0x00000001)
(def FXF_WRITE 0x00000002)
(def FXF_APPEND 0x00000004)
(def FXF_CREAT 0x00000008)
(def FXF_TRUNC 0x00000010)
(def FXF_EXCL 0x00000020)

; SFTP Status Codes (returned by libssh2_sftp_last_error() )
(def FX_OK 0)
(def FX_EOF 1)
(def FX_NO_SUCH_FILE 2)
(def FX_PERMISSION_DENIED 3)
(def FX_FAILURE 4)
(def FX_BAD_MESSAGE 5)
(def FX_NO_CONNECTION 6)
(def FX_CONNECTION_LOST 7)
(def FX_OP_UNSUPPORTED 8)
(def FX_INVALID_HANDLE 9)
(def FX_NO_SUCH_PATH 10)
(def FX_FILE_ALREADY_EXISTS 11)
(def FX_WRITE_PROTECT 12)
(def FX_NO_MEDIA 13)
(def FX_NO_SPACE_ON_FILESYSTEM 14)
(def FX_QUOTA_EXCEEDED 15)
(def FX_UNKNOWN_PRINCIPLE 16) ; Initial mis-spelling
(def FX_UNKNOWN_PRINCIPAL 16)
(def FX_LOCK_CONFlICT 17) ; Initial mis-spelling
(def FX_LOCK_CONFLICT 17)
(def FX_DIR_NOT_EMPTY 18)
(def FX_NOT_A_DIRECTORY 19)
(def FX_INVALID_FILENAME 20)
(def FX_LINK_LOOP 21)

(def ^{:arglists '([handle])} close-handle
  "int libssh2_sftp_close_handle(LIBSSH2_SFTP_HANDLE *handle);"
  (jna/to-fn Integer ssh2/libssh2_sftp_close_handle))

(defn close
  "int libssh2_sftp_close(LIBSSH2_SFTP_HANDLE *handle);"
  [handle]
  (close-handle handle))

(defn closedir
  "int libssh2_sftp_closedir(LIBSSH2_SFTP_HANDLE *handle);"
  [handle]
  (close-handle handle))

(def ^{:arglists '([handle attrs setstat])} fstat-ex
  "
   int libssh2_sftp_fstat_ex(LIBSSH2_SFTP_HANDLE *handle,
                             LIBSSH2_SFTP_ATTRIBUTES *attrs,
                             int setstat);"
  (jna/to-fn Integer ssh2/libssh2_sftp_fstat_ex))

(defn fsetstat
  "
   int libssh2_sftp_fsetstat(LIBSSH2_SFTP_HANDLE *handle,
                             LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [handle attrs]
  (fstat-ex handle attrs 1))

(defn fstat
  "
   int libssh2_sftp_fstat(LIBSSH2_SFTP_HANDLE *handle,
                          LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [handle attrs]
  (fstat-ex handle attrs 0))

(def ^{:arglists '([handle st])} fstatvfs
  "
   int libssh2_sftp_fstatvfs(LIBSSH2_SFTP_HANDLE *handle,
                             LIBSSH2_SFTP_STATVFS *st);"
  (jna/to-fn Integer ssh2/libssh2_sftp_fstatvfs))

(def ^{:arglists '([handle])} fsync
  "int libssh2_sftp_fsync(LIBSSH2_SFTP_HANDLE *handle);"
  (jna/to-fn Integer ssh2/libssh2_sftp_fsync))

(def ^{:arglists '([sftp])} get-channel
  "LIBSSH2_CHANNEL *libssh2_sftp_get_channel(LIBSSH2_SFTP *sftp);"
  (jna/to-fn Pointer ssh2/libssh2_sftp_get_channel))

(def ^{:arglists '([session])} init
  "LIBSSH2_SFTP *libssh2_sftp_init(LIBSSH2_SESSION *session);"
  (jna/to-fn Pointer ssh2/libssh2_sftp_init))

(def ^{:arglists '([sftp])} last-error
  "unsigned long libssh2_sftp_last_error(LIBSSH2_SFTP *sftp);"
  (jna/to-fn Long ssh2/libssh2_sftp_last_error))

(declare stat-ex)
(defn lstat
  "
   int libssh2_sftp_lstat(LIBSSH2_SFTP *sftp,
                          const char *path,
                          LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [sftp path attrs]
  (stat-ex sftp path (count path) LSTAT attrs))

(def ^{:arglists '([sftp path path-len mode])} mkdir-ex
  "
   int libssh2_sftp_mkdir_ex(LIBSSH2_SFTP *sftp,
                             const char *path,
                             unsigned int path_len, long mode);"
  (jna/to-fn Integer ssh2/libssh2_sftp_mkdir_ex))

(defn mkdir
  "
   int libssh2_sftp_mkdir(LIBSSH2_SFTP *sftp,
                          const char *path,
                          long mode);"
  [sftp path mode]
  (mkdir-ex sftp path (count path) mode))

(def ^{:arglists '([sftp filename filename-len flags mode open-type])} open-ex
  "
   LIBSSH2_SFTP_HANDLE *libssh2_sftp_open_ex(LIBSSH2_SFTP *sftp,
                                             const char *filename,
                                             unsigned int filename_len,
                                             unsigned long flags,
                                             long mode, int open_type);"
  (jna/to-fn Pointer ssh2/libssh2_sftp_open_ex))

(defn open
  "
   LIBSSH2_SFTP_HANDLE *libssh2_sftp_open(LIBSSH2_SFTP *sftp,
                                          const char *filename,
                                          unsigned long flags,
                                          long mode);"
  [sftp filename flags mode]
  (open-ex sftp filename (count filename) flags mode OPENFILE))

(defn opendir
  "LIBSSH2_SFTP_HANDLE *libssh2_sftp_open(LIBSSH2_SFTP *sftp, const char *path);"
  [sftp path]
  (open-ex sftp path (count path) 0 0 OPENDIR))

(def ^{:arglists '([handle buffer buffer-maxlen])} read
  "
   ssize_t libssh2_sftp_read(LIBSSH2_SFTP_HANDLE *handle,
                                      char *buffer, size_t buffer_maxlen);"
  (jna/to-fn Integer ssh2/libssh2_sftp_read))

(def ^{:arglists '([handle buffer buffer-maxlen longentry longentry-maxlen attrs])} readdir-ex
  "
   int libssh2_sftp_readdir_ex(LIBSSH2_SFTP_HANDLE *handle,
                               char *buffer, size_t buffer_maxlen,
                               char *longentry,
                               size_t longentry_maxlen,
                               LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  (jna/to-fn Integer ssh2/libssh2_sftp_readdir_ex))

(defn readdir
  "
   int libssh2_sftp_readdir_ex(LIBSSH2_SFTP_HANDLE *handle,
                               char *buffer, size_t buffer_maxlen,
                               char *longentry,
                               size_t longentry_maxlen,
                               LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [handle buffer buffer-maxlen attrs]
  (readdir-ex handle buffer buffer-maxlen nil 0 attrs))

(declare symlink-ex)
(defn readlink
  "
   int libssh2_sftp_readlink(LIBSSH2_SFTP *sftp,
                             const char *path,
                             char *target
                             unsigned int maxlen);"
  [sftp path target maxlen]
  (symlink-ex sftp path (count path) target maxlen READLINK))

(defn realpath
  "
   int libssh2_sftp_realpath(LIBSSH2_SFTP *sftp,
                             const char *path,
                             char *target
                             unsigned int maxlen);"
  [sftp path target maxlen]
  (symlink-ex sftp path (count path) target maxlen REALPATH))

(def ^{:arglists '([sftp source-filename source-filename-len dest-filename dest-filename-len flags])} rename-ex
  "
   int libssh2_sftp_rename_ex(LIBSSH2_SFTP *sftp,
                              const char *source_filename,
                              unsigned int srouce_filename_len,
                              const char *dest_filename,
                              unsigned int dest_filename_len,
                              long flags);"
  (jna/to-fn Integer ssh2/libssh2_sftp_rename_ex))

(defn rename
  "
   int libssh2_sftp_rename(LIBSSH2_SFTP *sftp,
                           const char *source_filename,
                           const char *dest_filename);"
  [sftp sourcefile destfile]
  (rename-ex sftp
             sourcefile (count sourcefile)
             destfile (count destfile)
             (bit-or RENAME_OVERWRITE RENAME_ATOMIC RENAME_NATIVE)))

(declare seek64)
(defn rewind
  "void libssh2_sftp_rewind(LIBSSH2_SFTP_HANDLE *handle)"
  [handle]
  (seek64 handle 0))

(def ^{:arglists '([sftp path path-len])} rmdir-ex
  "
   int libssh2_sftp_rmdir_ex(LIBSSH2_SFTP *sftp,
                             const char *path,
                             unsigned int path_len);"
  (jna/to-fn Integer ssh2/libssh2_sftp_rmdir_ex))

(defn rmdir
  "
   int libssh2_sftp_rmdir(LIBSSH2_SFTP *sftp,
                          const char *path,
                          unsigned int path_len);"
  [sftp path]
  (rmdir-ex sftp path (count path)))

(def ^{:arglists '([handle offset]) :deprecated "libssh2"} seek
  "void libssh2_sftp_seek(LIBSSH2_SFTP_HANDLE *handle, size_t offset);"
  (jna/to-fn Void ssh2/libssh2_sftp_seek))

(def ^{:arglists '([handle offset])} seek64
  "void libssh2_sftp_seek64(LIBSSH2_SFTP_HANDLE *handle, libssh2_uint64_t offset);"
  (jna/to-fn Void ssh2/libssh2_sftp_seek64))

(defn setstat
  "
   int libssh2_sftp_setstat(LIBSSH2_SFTP *sftp,
                            const char *path,
                            LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [sftp path attrs]
  (stat-ex sftp path (count path) SETSTAT attrs))

(def ^{:arglists '([sftp])} shutdown
  "int libssh2_sftp_shutdown(LIBSSH2_SFTP *sftp);"
  (jna/to-fn Integer ssh2/libssh2_sftp_shutdown))

(def ^{:arglists '([sftp path path-len stat-type attrs])} stat-ex
  "
   int libssh2_sftp_stat_ex(LIBSSH2_SFTP *sftp,
                            const char *path,
                            unsigned int path_len,
                            int stat_type,
                            LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  (jna/to-fn Integer ssh2/libssh2_sftp_stat_ex))

(defn stat
  "
   int libssh2_sftp_stat(LIBSSH2_SFTP *sftp,
                         const char *path,
                         LIBSSH2_SFTP_ATTRIBUTES *attrs);"
  [sftp path attrs]
  (stat-ex sftp path (count path) STAT attrs))

(def ^{:arglists '([sftp path path-len st])} statvfs
  "
   int libssh2_sftp_statvfs(LIBSSH2_SFTP *sftp,
                            const char *path,
                            size_t path_len,
                            LIBSSH2_SFTP_STATVFS *st);"
  (jna/to-fn Integer ssh2/libssh2_sftp_statvfs))

(def ^{:arglists '([sftp path path-len target target-len link-type])} symlink-ex
  "
   int libssh2_sftp_symlink_ex(LIBSSH2_SFTP *sftp,
                               const char *path,
                               unsigned int path_len,
                               char *target,
                               unsigned int target_len, int link_type);"
  (jna/to-fn Integer ssh2/libssh2_sftp_symlink_ex))

(defn symlink
  "
   int libssh2_sftp_symlink(LIBSSH2_SFTP *sftp,
                            const char *path,
                            char *target);"
  [sftp path target]
  (symlink-ex sftp path (count path) target (count target) SYMLINK))

(def ^{:arglists '([handle]) :deprecated "libssh2"} tell
  "size_t libssh2_sftp_tell(LIBSSH2_SFTP_HANDLE *handle);"
  (jna/to-fn Long ssh2/libssh2_sftp_tell))

(def ^{:arglists '([handle])} tell64
  "libssh2_uint64_t libssh2_sftp_tell64(LIBSSH2_SFTP_HANDLE *handle);"
  (jna/to-fn Long ssh2/libssh2_sftp_tell64))

(def ^{:arglists '([sftp filename filename-len])} unlink-ex
  "
   int libssh2_sftp_unlink_ex(LIBSSH2_SFTP *sftp,
                              const char *filename,
                              unsigned int filename_len);"
  (jna/to-fn Integer ssh2/libssh2_sftp_unlink_ex))

(defn unlink
  "int libssh2_sftp_unlink_ex(LIBSSH2_SFTP *sftp, const char *filename);"
  [sftp filename]
  (unlink-ex sftp filename (count filename)))

(def ^{:arglists '([handle buffer count])} write
  "
   ssize_t libssh2_sftp_write(LIBSSH2_SFTP_HANDLE *handle,
                              const char *buffer, size_t count);"
  (jna/to-fn Integer ssh2/libssh2_sftp_write))
