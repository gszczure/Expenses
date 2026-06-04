package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequestDto(
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Transaction type cannot be null")
        TransactionType type,

        @NotBlank(message = "Category cannot be blank")
        String category,

        String description,

        @NotNull(message = "Transaction date cannot be null")
        LocalDate transactionDate,

        @NotNull(message = "Account id cannot be null")
        Long accountId

) {
}
