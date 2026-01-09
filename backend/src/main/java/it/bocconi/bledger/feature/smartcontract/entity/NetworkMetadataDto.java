package it.bocconi.bledger.feature.smartcontract.entity;

import java.time.Instant;
import java.util.List;

public record NetworkMetadataDto(
        String networkId,
        Instant networkCreated,
        Instant networkUpdated,
        Instant metadataCreated,
        String merkleHash,
        Long membersCount,
        Long uploadedCount,
        Long auditedCount,
        List<DocumentMetadataDto> docs
) {
}
