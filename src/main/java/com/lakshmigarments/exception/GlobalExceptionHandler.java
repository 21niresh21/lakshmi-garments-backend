package com.lakshmigarments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(DuplicateSupplierException.class)
    public ResponseEntity<String> handleDuplicateSupplier(DuplicateSupplierException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
	
	@ExceptionHandler(DuplicateTransportException.class)
    public ResponseEntity<String> handleDuplicateTransport(DuplicateTransportException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
	
	@ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<String> handleDuplicateCategory(DuplicateCategoryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
	
	@ExceptionHandler(DuplicateSubCategoryException.class)
    public ResponseEntity<String> handleDuplicateSubCategory(DuplicateSubCategoryException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
	
	@ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<String> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<String> handleDuplicateUser(DuplicateUsernameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

}
