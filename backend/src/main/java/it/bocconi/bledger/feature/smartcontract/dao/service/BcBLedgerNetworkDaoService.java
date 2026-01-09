package it.bocconi.bledger.feature.smartcontract.dao.service;


import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.smartcontract.dao.repository.BcBLedgerNetworkMetadataRepository;
import it.bocconi.bledger.feature.smartcontract.entity.BcBLedgerNetworkMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BcBLedgerNetworkDaoService extends BcAbstractDaoService<BcBLedgerNetworkMetadata, BcBLedgerNetworkMetadataRepository> {

    public BcBLedgerNetworkDaoService(BcBLedgerNetworkMetadataRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }


    public Mono<BcBLedgerNetworkMetadata> findByContentHash(String hash) {
        return repository.findByContentHashAndDeletedFalse(hash);
    }
}
