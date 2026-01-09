package it.bocconi.bledger.feature.smartcontract.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import reactor.core.publisher.Mono;

public interface BcSmartContractRepository extends BcAbstractRepository<BcSmartContract> {
    Mono<BcSmartContract> findByType(SmartContractType smartContractType);

    Mono<Boolean> existsByType(SmartContractType smartContractType);
}
