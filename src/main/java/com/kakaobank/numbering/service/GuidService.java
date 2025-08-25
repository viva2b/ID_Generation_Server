package com.kakaobank.numbering.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GuidService {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String DEFAULT_SERVER_ID = "SV01";
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String serverId;
    
    public GuidService() {
        this.serverId = initializeServerId();
    }
    
    public String generateGuid() {
        // Timestamp (17 digits) - millisecond precision
        String timestamp = generateTimestamp();

        // Process ID (5 digits)
        long pid = ProcessHandle.current().pid();
        String processId = String.format("%05d", pid % 100000);
        
        // Counter (4 digits)
        int count = counter.getAndIncrement();
        if (count >= 10000) {
            counter.compareAndSet(count, 0);
            count = count % 10000;
        }
        String counterStr = String.format("%04d", count);
        
        return timestamp + serverId + processId + counterStr;
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
}