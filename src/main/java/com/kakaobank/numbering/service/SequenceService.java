package com.kakaobank.numbering.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

@Service
public class SequenceService {
    
    private static final Logger log = LoggerFactory.getLogger(SequenceService.class);
    private static final String KEY_PREFIX = "seq:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long MAX_SEQUENCE_VALUE = 9_999_999_999L;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public SequenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public Long generateSequence() {
        String key = getSequenceKey();
        
        try {
            Long sequence = performAtomicIncrement(key);
            
            validateSequenceRange(sequence);
            
            if (sequence == 1L) {
                setDailyExpiration(key);
            }
            
            log.debug("Generated sequence: {} for key: {}", sequence, key);
            return sequence;
            
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed while generating sequence", e);
            throw new RuntimeException("Unable to generate sequence: Redis connection failed", e);
        } catch (Exception e) {
            log.error("Unexpected error while generating sequence", e);
            throw new RuntimeException("Unable to generate sequence", e);
        }
    }
    
    private void setDailyExpiration(String key) {
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        Duration ttl = Duration.between(LocalDateTime.now(), tomorrow);
        redisTemplate.expire(key, ttl);
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