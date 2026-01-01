package com.lakshmigarments.exception;

public class JobworkItemNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public JobworkItemNotFoundException(String message) {
        super(message);
    }
}
