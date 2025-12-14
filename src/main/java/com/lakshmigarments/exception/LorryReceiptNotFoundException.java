package com.lakshmigarments.exception;

public class LorryReceiptNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public LorryReceiptNotFoundException(String message) {
        super(message);
    }

}
