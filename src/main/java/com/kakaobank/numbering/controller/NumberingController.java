package com.kakaobank.numbering.controller;

import com.kakaobank.numbering.dto.GuidResponse;
import com.kakaobank.numbering.dto.SequenceResponse;
import com.kakaobank.numbering.service.GuidService;
import com.kakaobank.numbering.service.SequenceService;
import org.springframework.web.bind.annotation.*;

@RestController
public class NumberingController {
    
    private final GuidService guidService;
    private final SequenceService sequenceService;
    
    public NumberingController(GuidService guidService, SequenceService sequenceService) {
        this.guidService = guidService;
        this.sequenceService = sequenceService;
    }
    
    @PostMapping("/guid")
    public GuidResponse generateGuid() {
        String guid = guidService.generateGuid();
        return new GuidResponse(guid);
    }
    
    @PostMapping("/sequence")
    public SequenceResponse generateSequence() {
        Long value = sequenceService.generateSequence();
        return new SequenceResponse(value);
    }
    
    @GetMapping("/sequence")
    public SequenceResponse getCurrentSequence() {
        Long value = sequenceService.getCurrentSequence();
        return new SequenceResponse(value);
    }
}