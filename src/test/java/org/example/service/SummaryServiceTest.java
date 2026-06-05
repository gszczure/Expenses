package org.example.service;

import org.example.dto.response.SummaryResponseDto;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Main account";
    private static final String FOOD_CATEGORY = "Food";
    private static final String TRANSPORT_CATEGORY = "Transport";

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @InjectMocks
    private SummaryService summaryService;

    @Test
    @DisplayName("Should return summary when transactions exist")
    void getSummary_shouldReturnSummary_whenTransactionsExist() {

        AccountTransaction income = createIncomeTransaction(BigDecimal.valueOf(1000));
        AccountTransaction foodExpense = createExpenseTransaction(BigDecimal.valueOf(200), FOOD_CATEGORY);
        AccountTransaction transportExpense = createExpenseTransaction(BigDecimal.valueOf(100), TRANSPORT_CATEGORY);

        when(accountTransactionRepository.findAll()).thenReturn(List.of(income, foodExpense, transportExpense));

        SummaryResponseDto result = summaryService.getSummary();

        assertThat(result.totalIncome()).isEqualByComparingTo("1000");
        assertThat(result.totalExpense()).isEqualByComparingTo("300");
        assertThat(result.expensesByCategory()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        FOOD_CATEGORY, BigDecimal.valueOf(200),
                        TRANSPORT_CATEGORY, BigDecimal.valueOf(100)));
    }

    @Test
    @DisplayName("Should return empty summary when transactions do not exist")
    void getSummary_shouldReturnEmptySummary_whenTransactionsDoNotExist() {

        when(accountTransactionRepository.findAll()).thenReturn(List.of());

        SummaryResponseDto result = summaryService.getSummary();

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.expensesByCategory()).isEmpty();
    }

    private AccountTransaction createIncomeTransaction(BigDecimal amount) {
        return AccountTransaction.builder()
                .id(1L)
                .amount(amount)
                .type(TransactionType.INCOME)
                .category("Salary")
                .description("Salary from work")
                .transactionDate(LocalDate.now())
                .account(createAccount())
                .build();
    }

    private AccountTransaction createExpenseTransaction(BigDecimal amount, String category) {
        return AccountTransaction.builder()
                .id(2L)
                .amount(amount)
                .type(TransactionType.EXPENSE)
                .category(category)
                .description("Expense")
                .transactionDate(LocalDate.now())
                .account(createAccount())
                .build();
    }

    private Account createAccount() {
        return Account.builder()
                .id(ACCOUNT_ID)
                .name(ACCOUNT_NAME)
                .balance(BigDecimal.ZERO)
                .build();
    }
}
