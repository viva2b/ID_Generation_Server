package com.kakaobank.numbering.exception;

public class GuidGenerationException extends RuntimeException {
    
    public GuidGenerationException(String message) {
        super(message);
    }
    
    public GuidGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}