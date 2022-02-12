package org.themullers.library.s3;

/**
 * Exception thrown when an unexpected error occurs in the object store access object.
 */
public class ObjectStoreException extends RuntimeException {
    public ObjectStoreException(String msg) {
        super(msg);
    }

    public ObjectStoreException(Throwable t) {
        super(t);
    }
}
