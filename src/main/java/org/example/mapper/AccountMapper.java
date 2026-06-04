package org.example.mapper;

import org.example.dto.response.AccountResponseDto;
import org.example.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponseDto mapToAccountResponseDto(Account account) {
        return new AccountResponseDto(
                account.getId(),
                account.getName(),
                account.getBalance()
        );
    }
}
