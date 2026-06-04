package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.SummaryResponseDto;
import org.example.model.AccountTransaction;
import org.example.model.TransactionType;
import org.example.repository.AccountTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final AccountTransactionRepository accountTransactionRepository;

    public SummaryResponseDto getSummary() {
        List<AccountTransaction> transactions = accountTransactionRepository.findAll();

        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpense = calculateTotalExpense(transactions);
        Map<String, BigDecimal> expensesByCategory = calculateExpensesByCategory(transactions);

        return new SummaryResponseDto(
                totalIncome,
                totalExpense,
                expensesByCategory
        );
    }

    private BigDecimal calculateTotalIncome(List<AccountTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(AccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalExpense(List<AccountTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(AccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> calculateExpensesByCategory(List<AccountTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        AccountTransaction::getCategory,
                        Collectors.mapping(
                                AccountTransaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
}
