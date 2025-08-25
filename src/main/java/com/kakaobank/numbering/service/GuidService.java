package com.kakaobank.numbering.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GuidService {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String DEFAULT_SERVER_ID = "SV01";
    private static final int MAX_COUNTER = 10000;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String serverId;
    private final String processId;
    
    public GuidService() {
        this.serverId = initializeServerId();
        this.processId = initializeProcessId();
    }
    
    public String generateGuid() {
        // Timestamp (17 digits) - millisecond precision
        String timestamp = generateTimestamp();
        
        // Counter (4 digits) with automatic overflow handling
        String counterStr = generateCounter();
        
        String guid = timestamp + serverId + processId + counterStr;
        
        // Validate GUID format (30 characters)
        validateGuidFormat(guid);
        
        return guid;
    }
    
    private String generateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(TIMESTAMP_FORMATTER);
    }
    
    private String initializeServerId() {
        String envServerId = System.getenv("SERVER_ID");
        if (envServerId != null && !envServerId.trim().isEmpty()) {
            if (envServerId.length() == 4) {
                return envServerId;
            }
            return String.format("%-4s", envServerId).substring(0, 4);
        }
        return DEFAULT_SERVER_ID;
    }
    
    private String initializeProcessId() {
        long pid = ProcessHandle.current().pid();
        return String.format("%05d", pid % 100000);
    }
    
    private String generateCounter() {
        int count = counter.getAndUpdate(current -> (current + 1) % MAX_COUNTER);
        return String.format("%04d", count);
    }
    
    private void validateGuidFormat(String guid) {
        if (guid == null || guid.length() != 30) {
            throw new IllegalStateException("Invalid GUID format: expected 30 characters, got " + 
                (guid == null ? "null" : guid.length()));
        }
    }
}