package com.kakaobank.numbering.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GuidService {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public String generateGuid() {
        // Timestamp (17 digits) - millisecond precision
        String timestamp = generateTimestamp();
        
        // Server ID (4 digits)
        String serverId = System.getenv("SERVER_ID");
        if (serverId == null || serverId.isEmpty()) {
            serverId = "SV01";
        }
        
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
}