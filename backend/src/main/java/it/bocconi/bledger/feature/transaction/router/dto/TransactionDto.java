package it.bocconi.bledger.feature.transaction.router.dto;

import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;

import java.time.LocalDateTime;

public record TransactionDto(
        String id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime confirmedAt,
        TransactionType transactionType,
        TransactionStatus status,
        String txHash
) {}
