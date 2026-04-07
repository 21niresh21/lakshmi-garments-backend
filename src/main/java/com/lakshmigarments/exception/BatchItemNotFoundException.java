package com.lakshmigarments.exception;

public class BatchItemNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BatchItemNotFoundException(String message) {
        super(message);
    }

}
