package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CreateAccountRequestDto;
import org.example.dto.response.AccountResponseDto;
import org.example.exception.AccountNotFoundException;
import org.example.exception.CannotDeleteAccountException;
import org.example.mapper.AccountMapper;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.repository.AccountTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::mapToAccountResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long accountId) {
        Account account = findAccount(accountId);

        return accountMapper.mapToAccountResponseDto(account);
    }

    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto request) {
        Account account = Account.builder()
                .name(request.name())
                .balance(BigDecimal.ZERO)
                .build();

        Account savedAccount = accountRepository.save(account);

        return accountMapper.mapToAccountResponseDto(savedAccount);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = findAccount(accountId);

        if (accountTransactionRepository.existsByAccountId(accountId)) {
            throw new CannotDeleteAccountException(
                    "Account with id " + accountId + " contains transactions and cannot be deleted");
        }

        accountRepository.delete(account);
    }

    public Account findAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() ->  new AccountNotFoundException("Account with id " + accountId + " not found"));
    }
}
