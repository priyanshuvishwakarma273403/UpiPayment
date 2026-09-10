package com.npci.exception;

public class NpciCommunicationException extends RuntimeException {
    public NpciCommunicationException(String message) {
        super(message);
    }
    public NpciCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
