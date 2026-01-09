package it.bocconi.bledger.feature.company.router.dto;

public record CompanyProfileImageDto (
       String filename,
       String mimeType,
       Long size,
       byte[] fileBinary
) {
}
