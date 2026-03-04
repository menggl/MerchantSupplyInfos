package com.msi.aop;

import com.msi.exception.SqlInjectionException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

@Aspect
@Component
public class SqlInjectionAspect {

    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionAspect.class);

    // Common SQL injection patterns
    // Be careful not to block legitimate words like 'select', 'update', 'insert' in normal text unless combined with SQL context
    private static final String SQL_INJECTION_REGEX = "(?i).*\\b(select|update|delete|insert|drop|truncate|alter|create|grant|revoke|union|exec|execute|waitfor|xp_cmdshell)\\b.*";
    // More specific patterns for SQL logic manipulation
    private static final String LOGIC_INJECTION_REGEX = "(?i).*('|%27).*\\b(or|and)\\b.+(=|like|>|<|in|between|is).*";
    
    private static final Pattern PATTERN_SQL = Pattern.compile(SQL_INJECTION_REGEX);
    private static final Pattern PATTERN_LOGIC = Pattern.compile(LOGIC_INJECTION_REGEX);

    @Pointcut("execution(* com.msi.controller..*(..))")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void checkSqlInjection(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        for (Object arg : args) {
            checkArgument(arg);
        }
    }

    private void checkArgument(Object arg) {
        if (arg == null) {
            return;
        }

        if (arg instanceof String) {
            checkString((String) arg);
        } else if (arg instanceof Integer || arg instanceof Long || arg instanceof Boolean || arg instanceof Double || arg instanceof Float) {
            // Primitives are safe usually
        } else {
            // Check object fields recursively (shallow check for simplicity)
            checkObjectFields(arg);
        }
    }

    private void checkObjectFields(Object obj) {
        if (obj == null) {
            return;
        }
        
        // Skip java/spring system classes
        String className = obj.getClass().getName();
        if (className.startsWith("java.") || className.startsWith("javax.") || 
            className.startsWith("org.springframework.") || className.startsWith("org.apache.")) {
            return;
        }

        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof String) {
                    checkString((String) value);
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
            logger.debug("Error checking object fields for SQL injection: {}", e.getMessage());
        }
    }

    private void checkString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        // Basic check for dangerous characters combined with SQL keywords
        if (PATTERN_LOGIC.matcher(value).matches()) {
            throw new SqlInjectionException("Potential SQL logic injection detected in input: " + value);
        }
        
        // Strict check for SQL command keywords if they appear in a way that looks like code
        // Simple 'select' word is common, so we check for suspicious context like 'union select'
        if (value.toLowerCase().contains("union select") || 
            value.toLowerCase().contains("drop table") ||
            value.toLowerCase().contains("exec(") ||
            value.toLowerCase().contains("waitfor delay")) {
            throw new SqlInjectionException("Dangerous SQL command detected in input: " + value);
        }
    }
}