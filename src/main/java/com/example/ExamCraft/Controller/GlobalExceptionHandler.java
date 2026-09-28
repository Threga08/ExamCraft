package com.example.ExamCraft.Controller;

import com.example.ExamCraft.exception.InsufficientQuestionsException;
import com.example.ExamCraft.exception.InvalidRequestException;
import com.example.ExamCraft.exception.ResourceNotFoundException;
import com.example.ExamCraft.exception.DuplicateSelectionException;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class,
            MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception exception) {
        String message = exception.getMessage();
        if (exception instanceof MethodArgumentNotValidException validationException) {
            message = validationException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .findFirst().orElse("Request validation failed.");
        } else if (exception instanceof HttpMessageNotReadableException) {
            message = "Request body is invalid or contains an unsupported value.";
        }
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(InsufficientQuestionsException.class)
    public ResponseEntity<Map<String, Object>> conflict(InsufficientQuestionsException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DuplicateSelectionException.class)
    public ResponseEntity<Map<String, Object>> duplicateSelection(DuplicateSelectionException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}