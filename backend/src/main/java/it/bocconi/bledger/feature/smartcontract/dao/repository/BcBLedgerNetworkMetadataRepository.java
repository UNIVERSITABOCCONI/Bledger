package it.bocconi.bledger.feature.smartcontract.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.smartcontract.entity.BcBLedgerNetworkMetadata;
import reactor.core.publisher.Mono;

public interface BcBLedgerNetworkMetadataRepository extends BcAbstractRepository<BcBLedgerNetworkMetadata> {
    Mono<BcBLedgerNetworkMetadata> findByNetworkIdAndDeletedFalse(String networkId);

    Mono<BcBLedgerNetworkMetadata> findByContentHashAndDeletedFalse(String hash);
}
