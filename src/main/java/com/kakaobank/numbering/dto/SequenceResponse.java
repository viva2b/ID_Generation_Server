package com.kakaobank.numbering.dto;

public class SequenceResponse {
    private Long value;
    
    public SequenceResponse() {}
    
    public SequenceResponse(Long value) {
        this.value = value;
    }
    
    public Long getValue() {
        return value;
    }
    
    public void setValue(Long value) {
        this.value = value;
    }
}