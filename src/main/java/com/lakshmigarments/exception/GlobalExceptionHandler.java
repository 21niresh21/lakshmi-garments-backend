package com.lakshmigarments.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.lakshmigarments.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(CategoryNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//    
//    @ExceptionHandler(InsufficientBatchQuantityException.class)
//    public ResponseEntity<ErrorResponse> handleInsufficientBatchQuantity(InsufficientBatchQuantityException ex, WebRequest request) {
//        return buildErrorResponse(ex, "INSUFFICIENT_BATCH_QUANTITY", HttpStatus.CONFLICT, request);
//    }
//
//    // jobwork not found exception
//    @ExceptionHandler(JobworkNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleJobworkNotFound(JobworkNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "JOBWORK_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    //supplier not found exception
//    @ExceptionHandler(SupplierNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleSupplierNotFound(SupplierNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "SUPPLIER_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // method argument not valid exception
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
//        Map<String, String> validationErrors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            validationErrors.put(fieldName, errorMessage);
//        });
//
//        ErrorResponse errorResponse = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
//                .errorCode("VALIDATION_FAILED")
//                .message("Validation failed for one or more fields")
//                .path(request.getDescription(false).replace("uri=", ""))
//                .validationErrors(validationErrors)
//                .build();
//
//        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//    }
//
//    // http message not readable exception
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
//        return buildErrorResponse(ex, "INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST, request, "Invalid request body");
//    }
//
//    // employee not found exception
//    @ExceptionHandler(EmployeeNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "EMPLOYEE_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // skill not found exception
//    @ExceptionHandler(SkillNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleSkillNotFound(SkillNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "SKILL_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // duplicate employee exception
//    @ExceptionHandler(DuplicateEmployeeException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateEmployee(DuplicateEmployeeException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_EMPLOYEE", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//    
//    @ExceptionHandler(DuplicateInvoiceException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateInvoice(DuplicateInvoiceException ex, WebRequest request) {
//        return buildErrorResponse(ex, "DUPLICATE_INVOICE", HttpStatus.CONFLICT, request);
//    }
//
//    // duplicate skill exception
//    @ExceptionHandler(DuplicateSkillException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateSkill(DuplicateSkillException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_SKILL", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//
//    // inventory not found exception
//    @ExceptionHandler(InventoryNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleInventoryNotFound(InventoryNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "INVENTORY_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // inventory not sufficient exception
//    @ExceptionHandler(InsufficientInventoryException.class)
//    public ResponseEntity<ErrorResponse> handleInsufficientInventory(InsufficientInventoryException ex, WebRequest request) {
//        return buildErrorResponse(ex, "INSUFFICIENT_INVENTORY", HttpStatus.BAD_REQUEST, request);
//    }
//
//    // sub category not found exception
//    @ExceptionHandler(SubCategoryNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleSubCategoryNotFound(SubCategoryNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "SUBCATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // batch status not found exception
//    @ExceptionHandler(BatchStatusNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleBatchStatusNotFound(BatchStatusNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "BATCH_STATUS_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    // duplicate batch exception
//    @ExceptionHandler(DuplicateBatchException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateBatch(DuplicateBatchException ex, WebRequest request) {
//        return buildErrorResponse(ex, "DUPLICATE_BATCH", HttpStatus.CONFLICT, request);
//    }
//
//    @ExceptionHandler(DuplicateSupplierException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateSupplier(DuplicateSupplierException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_SUPPLIER", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//
//    @ExceptionHandler(DuplicateTransportException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateTransport(DuplicateTransportException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_TRANSPORT", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//
//    @ExceptionHandler(DuplicateCategoryException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateCategory(DuplicateCategoryException ex, WebRequest request) {
//        String field = ex.getMessage().toLowerCase().contains("code") ? "code" : "name";
//        return buildValidationErrorResponse(ex, "DUPLICATE_CATEGORY", HttpStatus.CONFLICT, request, Map.of(field, ex.getMessage()));
//    }
//
//    @ExceptionHandler(DuplicateSubCategoryException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateSubCategory(DuplicateSubCategoryException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_SUBCATEGORY", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//
//    @ExceptionHandler(RoleNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "ROLE_NOT_FOUND", HttpStatus.BAD_REQUEST, request);
//    }
//
//    @ExceptionHandler(DuplicateUsernameException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicateUsernameException ex, WebRequest request) {
//        Map<String, String> validationErrors = Map.of("username", ex.getMessage());
//        
//        ErrorResponse errorResponse = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.CONFLICT.value())
//                .error(HttpStatus.CONFLICT.getReasonPhrase())
//                .errorCode("DUPLICATE_USERNAME")
//                .message(ex.getMessage())
//                .path(request.getDescription(false).replace("uri=", ""))
//                .validationErrors(validationErrors)
//                .build();
//                
//        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
//    }
//
//    @ExceptionHandler(DuplicateItemException.class)
//    public ResponseEntity<ErrorResponse> handleDuplicateItem(DuplicateItemException ex, WebRequest request) {
//        return buildValidationErrorResponse(ex, "DUPLICATE_ITEM", HttpStatus.CONFLICT, request, Map.of("name", ex.getMessage()));
//    }
//
//    @ExceptionHandler(ItemNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
//        return buildErrorResponse(ex, "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, request, "Invalid credentials.");
//    }
//
//    @ExceptionHandler(DisabledException.class)
//    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, WebRequest request) {
//        return buildErrorResponse(ex, "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN, request, "Access revoked. Contact admin.");
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
//        return buildErrorResponse(ex, "ACCESS_DENIED", HttpStatus.FORBIDDEN, request, "You do not have permission to access this resource.");
//    }
//
//    @ExceptionHandler(BusinessRuleViolationException.class)
//    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleViolationException ex, WebRequest request) {
//        return buildErrorResponse(ex, ex.getErrorCode(), HttpStatus.BAD_REQUEST, request);
//    }
//
//    @ExceptionHandler(TransactionSystemException.class)
//    public ResponseEntity<ErrorResponse> handleTransactionSystem(TransactionSystemException ex, WebRequest request) {
//        // Unwrap to see if it's an optimistic locking or constraint error
//        Throwable cause = ex.getRootCause();
//        if (cause instanceof org.hibernate.StaleObjectStateException || cause instanceof jakarta.persistence.OptimisticLockException) {
//            return buildErrorResponse(ex, "CONCURRENCY_ERROR", HttpStatus.CONFLICT, request, "The record has been updated by another user. Please refresh.");
//        }
//        return buildErrorResponse(ex, "TRANSACTION_FAILURE", HttpStatus.INTERNAL_SERVER_ERROR, request, "Transaction failed to commit.");
//    }
//
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
//        return buildErrorResponse(ex, "USER_NOT_FOUND", HttpStatus.NOT_FOUND, request);
//    }
//
//    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
//    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {
//        return buildErrorResponse(ex, "CONCURRENCY_ERROR", HttpStatus.CONFLICT, request, "The record has been updated by another user. Please refresh and try again.");
//    }
//
//    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
//    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
//        String message = "Database integrity violation";
//        String errorCode = "DATA_INTEGRITY_VIOLATION";
//        
//        if (ex.getMostSpecificCause().getMessage().contains("Duplicate entry")) {
//            message = "A record with this information already exists.";
//            errorCode = "DUPLICATE_ENTRY";
//        }
//        
//        return buildErrorResponse(ex, errorCode, HttpStatus.CONFLICT, request, message);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
//        return buildErrorResponse(ex, "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, request, "An unexpected error occurred");
//    }
//
//    private ResponseEntity<ErrorResponse> buildValidationErrorResponse(Exception ex, String errorCode, HttpStatus status, WebRequest request, Map<String, String> validationErrors) {
//        ErrorResponse errorResponse = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(status.value())
//                .error(status.getReasonPhrase())
//                .errorCode(errorCode)
//                .message(ex.getMessage())
//                .path(request.getDescription(false).replace("uri=", ""))
//                .validationErrors(validationErrors)
//                .build();
//        return new ResponseEntity<>(errorResponse, status);
//    }
//
//    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, String errorCode, HttpStatus status, WebRequest request) {
//        return buildErrorResponse(ex, errorCode, status, request, ex.getMessage());
//    }
//
//    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, String errorCode, HttpStatus status, WebRequest request, String message) {
//        ErrorResponse errorResponse = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(status.value())
//                .error(status.getReasonPhrase())
//                .errorCode(errorCode)
//                .message(message)
//                .path(request.getDescription(false).replace("uri=", ""))
//                .build();
//        return new ResponseEntity<>(errorResponse, status);
//    }
}
