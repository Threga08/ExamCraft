package com.example.ExamCraft.exception;

public class InsufficientQuestionsException extends RuntimeException {
    public InsufficientQuestionsException(String message) {
        super(message);
    }
}
