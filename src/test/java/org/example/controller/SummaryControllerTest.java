package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.response.SummaryResponseDto;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.service.SummaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SummaryController.class)
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SummaryService summaryService;

    @Test
    @DisplayName("Should return summary when summary exists")
    void getSummary_shouldReturnSummary_whenSummaryExists() throws Exception {

        when(summaryService.getSummary()).thenReturn(createSummaryResponseDto());

        mockMvc.perform(get("/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000))
                .andExpect(jsonPath("$.totalExpense").value(300))
                .andExpect(jsonPath("$.expensesByCategory.Food").value(200))
                .andExpect(jsonPath("$.expensesByCategory.Transport").value(100));
    }

    private SummaryResponseDto createSummaryResponseDto() {
        return new SummaryResponseDto(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300),
                Map.of(
                        "Food", BigDecimal.valueOf(200),
                        "Transport", BigDecimal.valueOf(100)
                )
        );
    }
}
