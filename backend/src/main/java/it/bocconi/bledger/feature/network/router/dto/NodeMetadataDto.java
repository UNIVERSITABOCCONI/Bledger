package it.bocconi.bledger.feature.network.router.dto;

public record NodeMetadataDto(
        String metadataKey,
        String metadataValue,
        String unitOfMeasure
) {}
