package org.example.dto.response;

import java.math.BigDecimal;

public record AccountResponseDto(
        Long id,
        String name,
        BigDecimal balance
) {
}
