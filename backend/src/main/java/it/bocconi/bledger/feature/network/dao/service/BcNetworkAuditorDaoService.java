package it.bocconi.bledger.feature.network.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.company.dao.row.CompanyLightRow;
import it.bocconi.bledger.feature.network.entity.BcNetworkAuditor;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkAuditorRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class BcNetworkAuditorDaoService extends BcAbstractDaoService<BcNetworkAuditor, BcNetworkAuditorRepository> {
    public BcNetworkAuditorDaoService(BcNetworkAuditorRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Flux<CompanyLightRow> findByCompanyRowLightByNetworkId(String networkId) {
        return repository.findCompanyLightRowByNetworkId(networkId);
    }

    public Mono<BcNetworkAuditor> findByNetworkIdAndCompanyId(String networkId, String companyId) {
        return repository.findByNetworkIdAndAuditorIdAndDeletedFalse(networkId, companyId);
    }

    public Mono<Integer> incrementRequestsCount(String id) {
        return repository.incrementRequestsCount(id);
    }

    public Mono<Integer> incrementAuditedCount(String id) {
        return repository.incrementAuditedCount(id);
    }

    public Flux<String> findAuditorAddressedByNetworkId(String networkId) {
        return repository.findByNetworkIdAndDeletedFalse(networkId);
    }

    public Mono<Integer> reloadAllCounts(String networkId) {
        return repository.reloadCountsByNetworkId(networkId);
    }
}
