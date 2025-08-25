package com.kakaobank.numbering.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class GuidServiceTest {
    
    private GuidService guidService;
    
    @BeforeEach
    void setUp() {
        guidService = new GuidService();
    }
    
    @Test
    @DisplayName("GUID는 정확히 30자리여야 한다")
    void guid_length_should_be_30() {
        String guid = guidService.generateGuid();
        
        assertThat(guid).hasSize(30);
    }
    
    @Test
    @DisplayName("GUID 구조가 올바른 형식이어야 한다")
    void guid_should_have_correct_format() {
        String guid = guidService.generateGuid();
        
        String timestamp = guid.substring(0, 17);
        String serverId = guid.substring(17, 21);
        String processId = guid.substring(21, 26);
        String counter = guid.substring(26, 30);
        
        assertThat(timestamp).matches("\\d{17}");
        assertThat(serverId).hasSize(4);
        assertThat(processId).matches("\\d{5}");
        assertThat(counter).matches("\\d{4}");
    }
    
    @Test
    @DisplayName("연속 생성된 GUID는 카운터가 증가해야 한다")
    void consecutive_guids_should_have_incremented_counter() {
        String guid1 = guidService.generateGuid();
        String guid2 = guidService.generateGuid();
        
        String counter1 = guid1.substring(26, 30);
        String counter2 = guid2.substring(26, 30);
        
        int count1 = Integer.parseInt(counter1);
        int count2 = Integer.parseInt(counter2);
        
        assertThat(count2).isEqualTo(count1 + 1);
    }
    
    @Test
    @DisplayName("동시에 생성된 GUID는 모두 유일해야 한다")
    void concurrent_guids_should_be_unique() throws InterruptedException {
        int threadCount = 100;
        int guidsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<String> guids = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < guidsPerThread; j++) {
                        String guid = guidService.generateGuid();
                        synchronized (guids) {
                            guids.add(guid);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertThat(guids).hasSize(threadCount * guidsPerThread);
    }
}