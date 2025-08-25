package com.kakaobank.numbering.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SequenceService {
    
    private static final String KEY_PREFIX = "seq:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long MAX_SEQUENCE_VALUE = 9_999_999_999L;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public SequenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public Long generateSequence() {
        String key = getSequenceKey();
        Long sequence = performAtomicIncrement(key);
        
        validateSequenceRange(sequence);
        
        return sequence;
    }
    
    private void validateSequenceRange(Long sequence) {
        if (sequence == null) {
            throw new IllegalStateException("Failed to generate sequence");
        }
        if (sequence > MAX_SEQUENCE_VALUE) {
            throw new IllegalStateException(
                String.format("Sequence exceeded maximum value: %d (max: %d)", 
                    sequence, MAX_SEQUENCE_VALUE));
        }
    }
    
    private Long performAtomicIncrement(String key) {
        return redisTemplate.opsForValue().increment(key);
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