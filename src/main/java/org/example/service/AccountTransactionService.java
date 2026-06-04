package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CreateTransactionRequestDto;
import org.example.dto.response.TransactionResponseDto;
import org.example.exception.TransactionNotFoundException;
import org.example.mapper.AccountTransactionMapper;
import org.example.model.Account;
import org.example.model.AccountTransaction;
import org.example.model.TransactionType;
import org.example.repository.AccountTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTransactionService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountTransactionMapper accountTransactionMapper;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactions() {
        return accountTransactionRepository.findAll()
                .stream()
                .map(accountTransactionMapper::mapToAccountTransactionResponseDto)
                .toList();
    }

    @Transactional
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto request) {
        Account account = accountService.findAccount(request.accountId());

        AccountTransaction transaction = AccountTransaction.builder()
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .account(account)
                .build();

        updateAccountBalance(account, transaction.getType(), transaction.getAmount());

        AccountTransaction savedTransaction = accountTransactionRepository.save(transaction);

        return accountTransactionMapper.mapToAccountTransactionResponseDto(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long transactionId) {
        AccountTransaction transaction = findTransaction(transactionId);

        updateAccountBalance(transaction.getAccount(), reverse(transaction.getType()), transaction.getAmount());

        accountTransactionRepository.delete(transaction);
    }

    private TransactionType reverse(TransactionType transactionType) {
        return transactionType == TransactionType.INCOME
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;
    }

    private void updateAccountBalance(Account account, TransactionType transactionType, BigDecimal amount) {

        if (transactionType == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
            return;
        }

        account.setBalance(account.getBalance().subtract(amount));
    }

    private AccountTransaction findTransaction(Long transactionId) {
        return accountTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction with id " + transactionId + " not found"));
    }
}
