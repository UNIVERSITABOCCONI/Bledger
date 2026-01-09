package it.bocconi.bledger.feature.company.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.company.dao.row.CompanyLightRow;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BcCompanyRepository extends BcAbstractRepository<BcCompany> {

    @Query("""
        SELECT c.id AS id, c.company_name AS company_name
        FROM bc_company c
        WHERE c.company_type = :type and c.deleted = false
    """)
    Flux<CompanyLightRow> findAllLightByType(CompanyType companyType);

    @Query("""
            select wallet.wallet_address from bc_company company
            join bc_wallet wallet on company.wallet_id = wallet.id
            where company.deleted = false and wallet.deleted = false and company.id = :id
            """)
    Mono<String> findCompanyWalletAddressById(String id);
    @Query("""
            select wallet.wallet_address from bc_company company
            join bc_wallet wallet on company.wallet_id = wallet.id
            where company.deleted = false and wallet.deleted = false and company.id in (:ids)
            """)
    Flux<String> findCompanyWalletAddressByIds(List<String> ids);
}
