package org.example.service;

import org.example.dto.request.CreateAccountRequestDto;
import org.example.dto.response.AccountResponseDto;
import org.example.exception.AccountNotFoundException;
import org.example.exception.CannotDeleteAccountException;
import org.example.mapper.AccountMapper;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.repository.AccountTransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Main account";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Should return all accounts when accounts exist")
    void getAccounts_shouldReturnAccounts_whenAccountsExist() {

        Account firstAccount = createAccount();
        Account secondAccount = createAccount();

        AccountResponseDto firstResponse = createAccountResponseDto();
        AccountResponseDto secondResponse = createAccountResponseDto();

        when(accountRepository.findAll()).thenReturn(List.of(firstAccount, secondAccount));
        when(accountMapper.mapToAccountResponseDto(firstAccount)).thenReturn(firstResponse);
        when(accountMapper.mapToAccountResponseDto(secondAccount)).thenReturn(secondResponse);

        List<AccountResponseDto> result = accountService.getAccounts();

        assertThat(result)
                .hasSize(2)
                .containsExactly(firstResponse, secondResponse);
    }

    @Test
    @DisplayName("Should return account when account exists")
    void getAccount_shouldReturnAccount_whenAccountExists() {

        Account account = createAccount();
        AccountResponseDto responseDto = createAccountResponseDto();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountMapper.mapToAccountResponseDto(account)).thenReturn(responseDto);

        AccountResponseDto result = accountService.getAccount(ACCOUNT_ID);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Should throw exception when account does not exist")
    void getAccount_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(ACCOUNT_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account with id " + ACCOUNT_ID + " not found");
    }

    @Test
    @DisplayName("Should create account when request is valid")
    void createAccount_shouldCreateAccount_whenRequestIsValid() {

        CreateAccountRequestDto request = createAccountRequestDto();
        Account savedAccount = createAccount();
        AccountResponseDto responseDto = createAccountResponseDto();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountMapper.mapToAccountResponseDto(savedAccount)).thenReturn(responseDto);

        AccountResponseDto result = accountService.createAccount(request);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();

        assertThat(capturedAccount.getName()).isEqualTo(ACCOUNT_NAME);
        assertThat(capturedAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    @DisplayName("Should delete account when account has no transactions")
    void deleteAccount_shouldDeleteAccount_whenAccountHasNoTransactions() {

        Account account = createAccount();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountTransactionRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(false);

        accountService.deleteAccount(ACCOUNT_ID);

        verify(accountRepository).delete(account);
    }

    @Test
    @DisplayName("Should throw exception when account contains transactions")
    void deleteAccount_shouldThrowCannotDeleteAccountException_whenAccountContainsTransactions() {

        Account account = createAccount();

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountTransactionRepository.existsByAccountId(ACCOUNT_ID)).thenReturn(true);

        assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_ID))
                .isInstanceOf(CannotDeleteAccountException.class)
                .hasMessage("Account with id " + ACCOUNT_ID + " contains transactions and cannot be deleted");
    }

    @Test
    @DisplayName("Should throw exception when account does not exist")
    void deleteAccount_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {

        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_ID))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account with id " + ACCOUNT_ID + " not found");
    }

    private Account createAccount() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .name(ACCOUNT_NAME)
                .balance(BigDecimal.valueOf(100))
                .build();
    }

    private AccountResponseDto createAccountResponseDto() {
        return new AccountResponseDto(
                ACCOUNT_ID,
                ACCOUNT_NAME,
                BigDecimal.valueOf(100));
    }

    private CreateAccountRequestDto createAccountRequestDto() {
        return new CreateAccountRequestDto(
                ACCOUNT_NAME
        );
    }
}
