package it.bocconi.bledger.feature.company.router.dto;

import it.bocconi.bledger.feature.company.enums.CompanyType;

public record CompanyDto (
        String id,
        String email,
        String nation,
        String companyName,
        String idType,
        String idNumber,
        String representativeName,
        String representativeSurname,
        String walletId,
        String walletAddress,
        CompanyType companyType
) {
}
