package it.bocconi.bledger.feature.network.router.dto;

import java.math.BigDecimal;

public record UpdateNodeValuesDto(
        BigDecimal quantity,
        BigDecimal transportEmissions
) {
}
