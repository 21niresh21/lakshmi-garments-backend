package com.lakshmigarments.exception;

public class BaleNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BaleNotFoundException(String message) {
        super(message);
    }

}
