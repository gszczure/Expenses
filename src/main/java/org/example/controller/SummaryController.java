package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.SummaryResponseDto;
import org.example.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponseDto> getSummary() {
        return ResponseEntity
                .ok(summaryService.getSummary());
    }
}
