package clj_libssh2.struct;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * A struct timespec. This can represent instants in time with up to nanosecond
 * precision depending on the underlying platform.
 */
public class Timespec
extends Structure
{
    public long tv_sec;
    public long tv_nsec;

    @Override
    public List<String> getFieldOrder() {
        return Arrays.asList("tv_sec", "tv_nsec");
    }

    /**
     * Re-interpret this struct as an {@link Instant} for more convenient use
     * in Java/Clojure.
     *
     * @return Instant  An {@link Instant} representing the same point in time
     *                  as the underlying struct.
     */
    public Instant toInstant() {
        return Instant.ofEpochSecond(tv_sec, tv_nsec);
    }

    @Override
    public String toString() {
        return toInstant().toString();
    }
}
