package it.bocconi.bledger.feature.network.router.dto;

import java.time.LocalDateTime;

public record GetAuditorRequestsDto(
        String id,
        String companyName,
        Integer nodeDepth,
        String auditFileTransactionId,
        LocalDateTime auditedAt
) {
}
