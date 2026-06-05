package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.CreateAccountRequestDto;
import org.example.dto.response.AccountResponseDto;
import org.example.exception.AccountNotFoundException;
import org.example.exception.CannotDeleteAccountException;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Main account";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("Should return accounts")
    void getAccounts_shouldReturnAccounts() throws Exception {

        when(accountService.getAccounts()).thenReturn(List.of(createAccountResponseDto()));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$[0].name").value(ACCOUNT_NAME))
                .andExpect(jsonPath("$[0].balance").value(100));
    }

    @Test
    @DisplayName("Should return account when account exists")
    void getAccount_shouldReturnAccount_whenAccountExists() throws Exception {

        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(createAccountResponseDto());

        mockMvc.perform(get("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.name").value(ACCOUNT_NAME))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    @DisplayName("Should create account when request is valid")
    void createAccount_shouldReturnCreatedAccount_whenRequestIsValid() throws Exception {

        CreateAccountRequestDto request = createAccountRequestDto();

        when(accountService.createAccount(any(CreateAccountRequestDto.class))).thenReturn(createAccountResponseDto());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.name").value(ACCOUNT_NAME))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    @DisplayName("Should return bad request when account name is blank")
    void createAccount_shouldReturnBadRequest_whenNameIsBlank() throws Exception {

        CreateAccountRequestDto request = new CreateAccountRequestDto("");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return no content when account exists")
    void deleteAccount_shouldReturnNoContent_whenAccountExists() throws Exception {

        mockMvc.perform(delete("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Should return not found when account does not exist")
    void deleteAccount_shouldReturnNotFound_whenAccountDoesNotExist() throws Exception {

        doThrow(new AccountNotFoundException("Account with id [1] not found"))
                .when(accountService)
                .deleteAccount(ACCOUNT_ID);

        mockMvc.perform(delete("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account with id [1] not found"));
    }

    @Test
    @DisplayName("Should return conflict when account contains transactions")
    void deleteAccount_shouldReturnConflict_whenAccountContainsTransactions() throws Exception {

        doThrow(new CannotDeleteAccountException("Account with id [1] contains transactions and cannot be deleted"))
                .when(accountService)
                .deleteAccount(ACCOUNT_ID);

        mockMvc.perform(delete("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Account with id [1] contains transactions and cannot be deleted"));
    }

    @Test
    @DisplayName("Should export transactions to csv when account exists")
    void exportTransactions_shouldReturnCsv_whenAccountExists() throws Exception {

        byte[] csv = """
                Id,Amount,Type,Category,Description,TransactionDate
                1,100.00,EXPENSE,Food,Pizza,2026-06-01
                """.getBytes(StandardCharsets.UTF_8);

        when(accountService.exportTransactions(ACCOUNT_ID)).thenReturn(csv);

        mockMvc.perform(get("/accounts/{id}/transactions/export", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=transactions.csv"))
                .andExpect(header().string("Content-Type",
                        containsString("text/csv")))
                .andExpect(content().string(
                        containsString("Id,Amount,Type,Category,Description,TransactionDate")))
                .andExpect(content().string(
                        containsString("1,100.00,EXPENSE,Food,Pizza,2026-06-01")));
    }

    private AccountResponseDto createAccountResponseDto() {
        return new AccountResponseDto(
                ACCOUNT_ID,
                ACCOUNT_NAME,
                BigDecimal.valueOf(100)
        );
    }

    private CreateAccountRequestDto createAccountRequestDto() {
        return new CreateAccountRequestDto(
                ACCOUNT_NAME
        );
    }
}
