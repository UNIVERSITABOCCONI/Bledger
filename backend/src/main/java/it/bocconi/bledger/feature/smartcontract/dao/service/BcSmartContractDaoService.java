package it.bocconi.bledger.feature.smartcontract.dao.service;


import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.smartcontract.dao.repository.BcSmartContractRepository;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BcSmartContractDaoService extends BcAbstractDaoService<BcSmartContract, BcSmartContractRepository> {

    public BcSmartContractDaoService(BcSmartContractRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Mono<BcSmartContract> findByType(SmartContractType smartContractType) {
        return repository.findByType(smartContractType);
    }

    public Mono<Boolean> existByType(SmartContractType smartContractType) {
        return repository.existsByType(smartContractType);
    }
}
