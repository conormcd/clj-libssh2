package clj_libssh2.struct;

/**
 * A marker exception for when we try to map a struct on a platform we
 * don't support yet.
 */
public class UnsupportedPlatformException
extends Exception
{
    public UnsupportedPlatformException(String message) {
        super(message);
    }
}
