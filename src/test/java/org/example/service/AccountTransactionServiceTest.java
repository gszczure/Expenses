package org.example.service;

import org.example.dto.request.CreateTransactionRequestDto;
import org.example.dto.response.TransactionResponseDto;
import org.example.exception.AccountNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.mapper.AccountTransactionMapper;
import org.example.model.Account;
import org.example.model.AccountTransaction;
import org.example.model.TransactionType;
import org.example.repository.AccountTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTransactionServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long TRANSACTION_ID = 1L;
    private static final String ACCOUNT_NAME = "Main account";
    private static final String CATEGORY = "Food";
    private static final String DESCRIPTION = "Lunch";

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @Mock
    private AccountTransactionMapper accountTransactionMapper;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountTransactionService accountTransactionService;

    @Test
    @DisplayName("Should return transactions when transactions exist")
    void getTransactions_shouldReturnTransactions_whenTransactionsExist() {

        AccountTransaction firstTransaction = createIncomeTransaction().build();
        AccountTransaction secondTransaction = createExpenseTransaction().build();
        TransactionResponseDto firstResponse = createTransactionResponseDto();
        TransactionResponseDto secondResponse = createTransactionResponseDto();

        when(accountTransactionRepository.findTransactions(null, null, null)).thenReturn(List.of
                (firstTransaction, secondTransaction));
        when(accountTransactionMapper.mapToAccountTransactionResponseDto(firstTransaction)).thenReturn(firstResponse);
        when(accountTransactionMapper.mapToAccountTransactionResponseDto(secondTransaction)).thenReturn(secondResponse);

        List<TransactionResponseDto> result = accountTransactionService.getTransactions(null, null,
                null);

        assertThat(result)
                .hasSize(2)
                .containsExactly(firstResponse, secondResponse);
    }

    @DisplayName("Should return transactions when category filter is provided")
    @Test
    void getTransactions_shouldReturnTransactions_whenCategoryFilterIsProvided() {

        String category = "Food";

        AccountTransaction transaction = createExpenseTransaction().category(category).build();
        TransactionResponseDto response = createTransactionResponseDto();

        when(accountTransactionRepository.findTransactions(null, null, category))
                .thenReturn(List.of(transaction));
        when(accountTransactionMapper.mapToAccountTransactionResponseDto(transaction)).thenReturn(response);

        List<TransactionResponseDto> result = accountTransactionService.getTransactions(null, null, category);

        assertThat(result)
                .hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when from date is after to date")
    void getTransactions_shouldThrowIllegalArgumentException_whenFromDateIsAfterToDate() {

        LocalDate from = LocalDate.of(2026, 12, 31);
        LocalDate to = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> accountTransactionService.getTransactions(from, to, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From date cannot be after to date");
    }

    @Test
    @DisplayName("Should create income transaction when transaction type is income")
    void createTransaction_shouldCreateIncomeTransaction_whenTransactionTypeIsIncome() {

        Account account = createAccount(BigDecimal.valueOf(100));
        CreateTransactionRequestDto request = createIncomeRequest();
        AccountTransaction transaction = createIncomeTransaction().build();
        TransactionResponseDto response = createTransactionResponseDto();

        when(accountService.findAccount(ACCOUNT_ID)).thenReturn(account);
        when(accountTransactionRepository.save(any(AccountTransaction.class))).thenReturn(transaction);
        when(accountTransactionMapper.mapToAccountTransactionResponseDto(transaction)).thenReturn(response);

        TransactionResponseDto result = accountTransactionService.createTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo("150");
        assertThat(result).isEqualTo(response);
        verify(accountTransactionRepository).save(any(AccountTransaction.class));
    }

    @Test
    @DisplayName("Should create expense transaction when transaction type is expense")
    void createTransaction_shouldCreateExpenseTransaction_whenTransactionTypeIsExpense() {

        Account account = createAccount(BigDecimal.valueOf(100));
        CreateTransactionRequestDto request = createExpenseRequest();
        AccountTransaction transaction = createExpenseTransaction().build();
        TransactionResponseDto response = createTransactionResponseDto();

        when(accountService.findAccount(ACCOUNT_ID)).thenReturn(account);
        when(accountTransactionRepository.save(any(AccountTransaction.class))).thenReturn(transaction);
        when(accountTransactionMapper.mapToAccountTransactionResponseDto(transaction)).thenReturn(response);

        TransactionResponseDto result = accountTransactionService.createTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo("60");
        assertThat(result).isEqualTo(response);
        verify(accountTransactionRepository).save(any(AccountTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when account does not exist")
    void createTransaction_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {

        CreateTransactionRequestDto request = createIncomeRequest();

        when(accountService.findAccount(ACCOUNT_ID))
                .thenThrow(new AccountNotFoundException("Account with id " + ACCOUNT_ID + " not found"));

        assertThatThrownBy(() -> accountTransactionService.createTransaction(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account with id " + ACCOUNT_ID + " not found");
    }

    @Test
    @DisplayName("Should delete income transaction and revert balance when transaction exists")
    void deleteTransaction_shouldDeleteIncomeTransactionAndRevertBalance_whenTransactionExists() {

        Account account = createAccount(BigDecimal.valueOf(150));
        AccountTransaction transaction = createIncomeTransaction().account(account).build();

        when(accountTransactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        accountTransactionService.deleteTransaction(TRANSACTION_ID);

        assertThat(account.getBalance()).isEqualByComparingTo("100");
        verify(accountTransactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should delete expense transaction and revert balance when transaction exists")
    void deleteTransaction_shouldDeleteExpenseTransactionAndRevertBalance_whenTransactionExists() {

        Account account = createAccount(BigDecimal.valueOf(60));
        AccountTransaction transaction = createExpenseTransaction().account(account).build();

        when(accountTransactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        accountTransactionService.deleteTransaction(TRANSACTION_ID);

        assertThat(account.getBalance()).isEqualByComparingTo("100");
        verify(accountTransactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should throw exception when transaction does not exist")
    void deleteTransaction_shouldThrowTransactionNotFoundException_whenTransactionDoesNotExist() {

        when(accountTransactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountTransactionService.deleteTransaction(TRANSACTION_ID))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction with id " + TRANSACTION_ID + " not found");
    }

    private Account createAccount(BigDecimal balance) {
        return Account.builder()
                .id(ACCOUNT_ID)
                .name(ACCOUNT_NAME)
                .balance(balance)
                .build();
    }

    private AccountTransaction.AccountTransactionBuilder createIncomeTransaction() {
        return AccountTransaction.builder()
                .id(TRANSACTION_ID)
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.INCOME)
                .category(CATEGORY)
                .description(DESCRIPTION)
                .transactionDate(LocalDate.now())
                .account(createAccount(BigDecimal.valueOf(100)));
    }

    private AccountTransaction.AccountTransactionBuilder createExpenseTransaction() {
        return AccountTransaction.builder()
                .id(TRANSACTION_ID)
                .amount(BigDecimal.valueOf(40))
                .type(TransactionType.EXPENSE)
                .category(CATEGORY)
                .description(DESCRIPTION)
                .transactionDate(LocalDate.now())
                .account(createAccount(BigDecimal.valueOf(100)));
    }

    private CreateTransactionRequestDto createIncomeRequest() {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(50),
                TransactionType.INCOME,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }

    private CreateTransactionRequestDto createExpenseRequest() {
        return new CreateTransactionRequestDto(
                BigDecimal.valueOf(40),
                TransactionType.EXPENSE,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }

    private TransactionResponseDto createTransactionResponseDto() {
        return new TransactionResponseDto(
                TRANSACTION_ID,
                BigDecimal.valueOf(50),
                TransactionType.INCOME,
                CATEGORY,
                DESCRIPTION,
                LocalDate.now(),
                ACCOUNT_ID
        );
    }
}
