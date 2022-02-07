package org.themullers.library.s3;

public class ObjectStoreException extends RuntimeException {
    public ObjectStoreException(String msg) {
        super(msg);
    }

    public ObjectStoreException(Throwable t) {
        super(t);
    }
}
