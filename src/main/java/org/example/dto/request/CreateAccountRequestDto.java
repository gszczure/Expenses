package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequestDto(
        @NotBlank(message = "Account name cannot be blank")
        String name
) {
}
