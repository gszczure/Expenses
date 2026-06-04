package org.example.mapper;

import org.example.dto.response.TransactionResponseDto;
import org.example.model.AccountTransaction;
import org.springframework.stereotype.Component;

@Component
public class AccountTransactionMapper {

    public TransactionResponseDto mapToAccountTransactionResponseDto(AccountTransaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getAccount().getId()
        );
    }
}
