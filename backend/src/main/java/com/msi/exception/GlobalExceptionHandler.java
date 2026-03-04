package com.msi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SqlInjectionException.class)
    public ResponseEntity<Map<String, Object>> handleSqlInjectionException(SqlInjectionException ex) {
        logger.warn("SQL INJECTION ATTEMPT DETECTED: {}", ex.getMessage());
        
        // Return a fake success response to mislead the attacker
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ok");
        // Add minimal data structure if needed to mimic success for some endpoints
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }
}