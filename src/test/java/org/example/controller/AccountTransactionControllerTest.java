package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.CreateTransactionRequestDto;
import org.example.dto.response.TransactionResponseDto;
import org.example.exception.AccountNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.model.TransactionType;
import org.example.service.AccountTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class AccountTransactionControllerTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long TRANSACTION_ID = 1L;
    private static final String CATEGORY = "Food";
    private static final String DESCRIPTION = "Lunch";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountTransactionService accountTransactionService;

    @Test
    @DisplayName("Should return transactions")
    void getTransactions_shouldReturnTransactions() throws Exception {

        when(accountTransactionService.getTransactions(null, null, null)).thenReturn(List.of(createTransactionResponseDto()));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TRANSACTION_ID))
                .andExpect(jsonPath("$[0].amount").value(100))
                .andExpect(jsonPath("$[0].type").value("INCOME"))
                .andExpect(jsonPath("$[0].category").value(CATEGORY));
    }

    @Test
    @DisplayName("Should create transaction when income request is valid")
    void createTransaction_shouldReturnCreatedTransaction_whenIncomeRequestIsValid() throws Exception {

        CreateTransactionRequestDto request = createIncomeRequest();

        when(accountTransactionService.createTransaction(any())).thenReturn(createTransactionResponseDto());

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TRANSACTION_ID))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @DisplayName("Should create transaction when expense request is valid")
    void createTransaction_shouldReturnCreatedTransaction_whenExpenseRequestIsValid() throws Exception {

        CreateTransactionRequestDto request = createExpenseRequest();

        TransactionResponseDto response = createExpenseResponseDto();

        when(accountTransactionService.createTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TRANSACTION_ID))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @ParameterizedTest
    @DisplayName("Should return bad request when request is invalid")
    @MethodSource("invalidRequests")
    void createTransaction_shouldReturnBadRequest_whenRequestIsInvalid(CreateTransactionRequestDto request)
            throws Exception {

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when transaction type is invalid")
    void createTransaction_shouldReturnBadRequest_whenTransactionTypeIsInvalid() throws Exception {

        String request = """
                {
                  "amount": 100,
                  "type": "INCOMMMEEE",
                  "category": "Food",
                  "description": "Lunch",
                  "transactionDate": "2026-06-04",
                  "accountId": 1
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return not found when account does not exist")
    void createTransaction_shouldReturnNotFound_whenAccountDoesNotExist() throws Exception {

        CreateTransactionRequestDto request = createIncomeRequest();

        when(accountTransactionService.createTransaction(any()))
                .thenThrow(new AccountNotFoundException("Account with id [1] not found"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account with id [1] not found"));
    }

    @Test
    @DisplayName("Should return no content when transaction exists")
    void deleteTransaction_shouldReturnNoContent_whenTransactionExists() throws Exception {

        mockMvc.perform(delete("/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isNoContent());

        verify(accountTransactionService).deleteTransaction(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Should return not found when transaction does not exist")
    void deleteTransaction_shouldReturnNotFound_whenTransactionDoesNotExist() throws Exception {

        doThrow(new TransactionNotFoundException("Transaction with id [1] not found"))
                .when(accountTransactionService)
                .deleteTransaction(TRANSACTION_ID);

        mockMvc.perform(delete("/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaction with id [1] not found"));
    }

    private static Stream<CreateTransactionRequestDto> invalidRequests() {
        return Stream.of(
                new CreateTransactionRequestDto(
                        BigDecimal.valueOf(-100),
                        TransactionType.INCOME,
                        CATEGORY,
                        DESCRIPTION,
                        LocalDate.now(),
                        ACCOUNT_ID
                ),
                new CreateTransactionRequestDto(
                        BigDecimal.valueOf(100),
                        TransactionType.INCOME,
                        "",
                        DESCRIPTION,
                        LocalDate.now(),
                        ACCOUNT_ID
                ),
                new CreateTransactionRequestDto(
                        BigDecimal.valueOf(100),
                        TransactionType.INCOME,
                        CATEGORY,
                        DESCRIPTION,
                        null,
                        ACCOUNT_ID
                )
        );
    }

    private TransactionResponseDto createTransactionResponseDto() {
        return new TransactionResponseDto(
                TRANSACTION_ID,
                BigDecimal.valueOf(100),
                TransactionType.INCOME,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }

    private TransactionResponseDto createExpenseResponseDto() {
        return new TransactionResponseDto(
                TRANSACTION_ID,
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }

    private CreateTransactionRequestDto createIncomeRequest() {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(100),
                TransactionType.INCOME,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }

    private CreateTransactionRequestDto createExpenseRequest() {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }
}
