package it.bocconi.bledger.feature.network.router.dto;

public record FileLightDto(
        String id,
        String name,
        String uploadTransactionId,
        Long size
) {}
