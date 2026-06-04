package org.example.dto.response;

import java.time.ZonedDateTime;

public record ErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
