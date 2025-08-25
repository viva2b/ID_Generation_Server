package com.kakaobank.numbering.dto;

public class GuidResponse {
    private String guid;
    
    public GuidResponse() {}
    
    public GuidResponse(String guid) {
        this.guid = guid;
    }
    
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
}