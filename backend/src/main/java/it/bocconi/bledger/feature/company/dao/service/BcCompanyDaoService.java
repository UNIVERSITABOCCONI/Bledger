package it.bocconi.bledger.feature.company.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.company.dao.repository.BcCompanyRepository;
import it.bocconi.bledger.feature.company.dao.row.CompanyLightRow;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BcCompanyDaoService extends BcAbstractDaoService<BcCompany, BcCompanyRepository> {
    public BcCompanyDaoService(BcCompanyRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    @Transactional(readOnly = true)
    public Flux<CompanyLightRow> getCompaniesByRolesRows(CompanyType companyType){
        return repository.findAllLightByType(companyType);
    }

    public Mono<String> getCompanyWalletAddressById(String id) {
        return repository.findCompanyWalletAddressById(id);
    }

    public Flux<String> getWalletAddressesByIds(List<String> list) {
        return repository.findCompanyWalletAddressByIds(list);
    }
}
