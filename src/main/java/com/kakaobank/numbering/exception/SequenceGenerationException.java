package com.kakaobank.numbering.exception;

public class SequenceGenerationException extends RuntimeException {
    
    public SequenceGenerationException(String message) {
        super(message);
    }
    
    public SequenceGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}