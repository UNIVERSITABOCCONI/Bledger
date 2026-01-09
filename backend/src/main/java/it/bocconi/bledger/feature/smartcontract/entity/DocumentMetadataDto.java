package it.bocconi.bledger.feature.smartcontract.entity;

import it.bocconi.bledger.feature.file.enums.FileType;

public record DocumentMetadataDto(
        String id,
        String nodeId,
        FileType type,
        String documentHash,
        boolean audited
) {
}
