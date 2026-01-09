package it.bocconi.bledger.feature.company.mapper;


import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.router.dto.CompanyDto;
import it.bocconi.bledger.feature.company.router.dto.CompanyProfileImageDto;
import it.bocconi.bledger.feature.file.dao.row.CompanyProfileImageRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    @Mapping(target = "walletAddress", source = "walletAddress")
    CompanyDto fromBcCompanyToCompanyDto(BcCompany company, String walletAddress);
    CompanyProfileImageDto companyProfileImageRowToCompanyProfileImageDto(CompanyProfileImageRow companyProfileImageRow);
}
