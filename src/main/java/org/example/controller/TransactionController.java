package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.CreateTransactionRequestDto;
import org.example.dto.response.TransactionResponseDto;
import org.example.service.AccountTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final AccountTransactionService accountTransactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(accountTransactionService.getTransactions(from, to, category));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody
                                                                        CreateTransactionRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountTransactionService.createTransaction(request));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        accountTransactionService.deleteTransaction(transactionId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
