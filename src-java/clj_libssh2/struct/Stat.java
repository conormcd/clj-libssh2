package clj_libssh2.struct;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Platform;
import com.sun.jna.Structure;

/**
 * A JNA mapping for struct stat.
 */
public abstract class Stat
extends Structure
{
    /**
     * Create a new struct stat. Since this structure varies between platforms
     * the returned object may behave quite differently depending on the OS.
     * While the returned object will expose all of its fields as public
     * members it's advisable to only use the methods exposed in the Stat class
     * itself.
     *
     * @return Stat An instance of this class which will map a struct stat from
     *              the underlying operating system.
     *
     * @throws UnsupportedPlatformException If we don't have an implementation
     *                                      for the current operating system.
     */
    public static Stat newInstance() throws UnsupportedPlatformException {
        switch (Platform.RESOURCE_PREFIX) {
            case "darwin":
                return new Darwin();
            case "linux-x86-64":
                return new Linux_X86_64();
            default:
                throw new UnsupportedPlatformException(Platform.RESOURCE_PREFIX);
        }
    }

    /**
     * Get the last access time of the described file.
     *
     * @return Instant The last access time.
     */
    public abstract Instant getATime();

    /**
     * Get the time the status of the described file was changed.
     *
     * @return Instant The value of ctime for the described file.
     */
    public abstract Instant getCTime();

    /**
     * The group ID assigned to the described file.
     *
     * @return int The file's GID.
     */
    public abstract int getGroupID();

    /**
     * Get the permissions mask for the described file.
     *
     * @return int An integer with the same permissions mask as the file's.
     */
    public abstract int getMode();

    /**
     * Get the last modification time of the described file.
     *
     * @return Instant The last time the file was modified.
     */
    public abstract Instant getMTime();

    /**
     * Get the size of the file in bytes.
     *
     * @return int The size of the described file, in bytes.
     */
    public abstract long getSize();

    /**
     * The UID of the user who owns the described file.
     *
     * @return int The user's UID.
     */
    public abstract int getUserID();

    /**
     * The implementation of struct stat for OSX.
     */
    public static class Darwin
    extends Stat
    {
        public int st_dev;                  // dev_t
        public short st_mode;               // mode_t
        public short st_nlink;              // nlink_t
        public long st_ino;                 // ino_t
        public int st_uid;                  // uid_t
        public int st_gid;                  // gid_t
        public int st_rdev;                 // dev_t
        public Timespec st_atimespec;       // struct timespec
        public Timespec st_mtimespec;       // struct timespec
        public Timespec st_ctimespec;       // struct timespec
        public Timespec st_birthtimespec;   // struct timespec
        public long st_size;                // off_t
        public long st_blocks;              // blkcnt_t
        public int st_blksize;              // blksize_t
        public int st_flags;                // uint32_t
        public int st_gen;                  // uint32_t
        public int st_lspare;               // int32_t
        public long st_qspare_1;            // int_64_t
        public long st_qspare_2;            // int_64_t

        @Override
        public Instant getATime() {
            return st_atimespec.toInstant();
        }

        @Override
        public Instant getCTime() {
            return st_ctimespec.toInstant();
        }

        @Override
        public int getGroupID() {
            return st_gid;
        }

        @Override
        public int getMode() {
            return st_mode;
        }

        @Override
        public Instant getMTime() {
            return st_mtimespec.toInstant();
        }

        @Override
        public long getSize() {
            return st_size;
        }

        @Override
        public int getUserID() {
            return st_uid;
        }

        @Override
        public List<String> getFieldOrder() {
            return Arrays.asList(
                "st_dev",
                "st_mode",
                "st_nlink",
                "st_ino",
                "st_uid",
                "st_gid",
                "st_rdev",
                "st_atimespec",
                "st_mtimespec",
                "st_ctimespec",
                "st_birthtimespec",
                "st_size",
                "st_blocks",
                "st_blksize",
                "st_flags",
                "st_gen",
                "st_lspare",
                "st_qspare_1",
                "st_qspare_2"
            );
        }
    }

    /**
     * The implementation of struct stat for Linux x86_64. The names and
     * mapping of the fields is from glibc.
     */
    public static class Linux_X86_64
    extends Stat
    {
        public long st_dev;         // dev_t
        public long st_ino;         // ino_t
        public long st_nlink;       // nlink_t
        public int st_mode;         // mode_t
        public int st_uid;          // uid_t
        public int st_gid;          // gid_t
        public int pad0;            // int
        public long st_rdev;        // dev_t
        public long st_size;        // off_t
        public long st_blksize;     // blksize_t
        public long st_blocks;      // blkcnt_t
        public Timespec st_atim;    // struct timespec
        public Timespec st_mtim;    // struct timespec
        public Timespec st_ctim;    // struct timespec
        public long reserved1;      // __syscall_slong_t
        public long reserved2;      // __syscall_slong_t
        public long reserved3;      // __syscall_slong_t

        @Override
        public Instant getATime() {
            return st_atim.toInstant();
        }

        @Override
        public Instant getCTime() {
            return st_ctim.toInstant();
        }

        @Override
        public int getGroupID() {
            return st_gid;
        }

        @Override
        public int getMode() {
            return st_mode;
        }

        @Override
        public Instant getMTime() {
            return st_mtim.toInstant();
        }

        @Override
        public long getSize() {
            return st_size;
        }

        @Override
        public int getUserID() {
            return st_uid;
        }

        @Override
        public List<String> getFieldOrder() {
            return Arrays.asList(
                "st_dev",
                "st_ino",
                "st_nlink",
                "st_mode",
                "st_uid",
                "st_gid",
                "pad0",
                "st_rdev",
                "st_size",
                "st_blksize",
                "st_blocks",
                "st_atim",
                "st_mtim",
                "st_ctim",
                "reserved1",
                "reserved2",
                "reserved3"
            );
        }
    }
}

