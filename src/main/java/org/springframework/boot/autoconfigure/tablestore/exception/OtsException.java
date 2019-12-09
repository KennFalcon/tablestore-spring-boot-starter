package org.springframework.boot.autoconfigure.tablestore.exception;

/**
 * @project: tablestore-spring-boot-starter
 * @description:
 * @author: Kenn
 * @create: 2019-12-06 14:23
 */
public class OtsException extends RuntimeException {

    public OtsException() {
        super();
    }

    public OtsException(String message) {
        super(message);
    }

    public OtsException(String format, Object... objects) {
        super(String.format(format, objects));
    }

    public OtsException(String message, Throwable cause) {
        super(message, cause);
    }

    public OtsException(String format, Throwable cause, Object... objects) {
        super(String.format(format, objects), cause);
    }

    public OtsException(Throwable cause) {
        super(cause);
    }
}
