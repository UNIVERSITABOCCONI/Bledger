package it.bocconi.bledger.feature.network.router.dto;

import java.util.List;

public record TpaNodeDetailsDto(
        FileLightDto scopeFile,
        FileLightDto granularScope1File,
        FileLightDto granularScope2File,
        List<NodeMetadataDto> extractedData
) {}
