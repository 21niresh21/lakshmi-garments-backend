package com.lakshmigarments.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler for JWT-related exceptions.
 * <p>
 * Handles common JWT errors such as expired tokens, malformed tokens, invalid signature,
 * unsupported tokens, and other generic exceptions by returning appropriate HTTP status codes
 * and error messages in the response body.
 * </p>
 */
@ControllerAdvice
public class JwtExceptionHandler {

    /**
     * Handles exceptions when a JWT token is expired.
     *
     * @param ex the ExpiredJwtException thrown during token validation.
     * @return a 401 Unauthorized response with error message indicating token expiration.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleExpiredKwtException(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token expired. Please login again."));
    }

    /**
     * Handles various invalid JWT token exceptions such as malformed token,
     * invalid signature, unsupported token, or illegal argument.
     *
     * @param ex the exception thrown during token parsing or validation.
     * @return a 401 Unauthorized response with error message indicating invalid token.
     */
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class, UnsupportedJwtException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleInvalidJwtException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid token. Please login again."));
    }
}
