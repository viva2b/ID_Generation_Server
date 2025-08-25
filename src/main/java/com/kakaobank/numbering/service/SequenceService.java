package com.kakaobank.numbering.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SequenceService {
    
    private static final String KEY_PREFIX = "seq:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public SequenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public Long generateSequence() {
        String key = getSequenceKey();
        Long sequence = redisTemplate.opsForValue().increment(key);
        
        // 범위 체크 (1 ~ 9,999,999,999)
        if (sequence != null && sequence > 9999999999L) {
            throw new IllegalStateException("Sequence exceeded maximum value: " + sequence);
        }
        
        return sequence;
    }
    
    public Long getCurrentSequence() {
        String key = getSequenceKey();
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
    
    private String getSequenceKey() {
        return generateDailyKey(LocalDate.now());
    }
    
    private String generateDailyKey(LocalDate date) {
        return KEY_PREFIX + date.format(DATE_FORMATTER);
    }
}