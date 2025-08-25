package com.kakaobank.numbering.service;

import com.kakaobank.numbering.exception.SequenceGenerationException;
import com.kakaobank.numbering.exception.RedisOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SequenceServiceTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    private SequenceService sequenceService;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        sequenceService = new SequenceService(redisTemplate);
    }
    
    @Test
    @DisplayName("Sequence 생성 시 Redis INCR이 호출되어야 한다")
    void should_call_redis_increment() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        Long sequence = sequenceService.generateSequence();
        
        assertThat(sequence).isEqualTo(1L);
        verify(valueOperations).increment(anyString());
    }
    
    @Test
    @DisplayName("첫 번째 Sequence 생성 시 TTL이 설정되어야 한다")
    void should_set_ttl_for_first_sequence() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);
        
        sequenceService.generateSequence();
        
        verify(redisTemplate).expire(anyString(), any());
    }
    
    @Test
    @DisplayName("두 번째 이후 Sequence 생성 시 TTL이 설정되지 않아야 한다")
    void should_not_set_ttl_for_subsequent_sequence() {
        when(valueOperations.increment(anyString())).thenReturn(2L);
        
        sequenceService.generateSequence();
        
        verify(redisTemplate, times(0)).expire(anyString(), any());
    }
    
    @Test
    @DisplayName("Sequence가 최대값을 초과하면 예외가 발생해야 한다")
    void should_throw_exception_when_sequence_exceeds_max() {
        when(valueOperations.increment(anyString())).thenReturn(10_000_000_000L);
        
        assertThatThrownBy(() -> sequenceService.generateSequence())
            .isInstanceOf(SequenceGenerationException.class)
            .hasMessageContaining("exceeded maximum value");
    }
    
    @Test
    @DisplayName("Redis 연결 실패 시 RedisOperationException이 발생해야 한다")
    void should_throw_exception_on_redis_connection_failure() {
        when(valueOperations.increment(anyString()))
            .thenThrow(new RedisConnectionFailureException("Connection failed"));
        
        assertThatThrownBy(() -> sequenceService.generateSequence())
            .isInstanceOf(RedisOperationException.class)
            .hasMessageContaining("Redis connection failed");
    }
    
    @Test
    @DisplayName("현재 Sequence 조회 시 값이 없으면 0을 반환해야 한다")
    void should_return_zero_when_no_current_sequence() {
        when(valueOperations.get(anyString())).thenReturn(null);
        
        Long currentSequence = sequenceService.getCurrentSequence();
        
        assertThat(currentSequence).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("현재 Sequence 조회 시 저장된 값을 반환해야 한다")
    void should_return_current_sequence_value() {
        when(valueOperations.get(anyString())).thenReturn("42");
        
        Long currentSequence = sequenceService.getCurrentSequence();
        
        assertThat(currentSequence).isEqualTo(42L);
    }
    
    @Test
    @DisplayName("Sequence 키는 날짜 기반으로 생성되어야 한다")
    void should_generate_date_based_key() {
        String expectedKey = "seq:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        when(valueOperations.increment(expectedKey)).thenReturn(1L);
        
        sequenceService.generateSequence();
        
        verify(valueOperations).increment(expectedKey);
    }
}