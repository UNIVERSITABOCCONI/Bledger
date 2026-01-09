package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.smartcontract.dao.repository.BcBLedgerNetworkMetadataRepository;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcBLedgerNetworkDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcBLedgerNetworkMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcBLedgerNetworkDaoServiceTest extends BcAbstractDaoServiceTest<BcBLedgerNetworkMetadata, BcBLedgerNetworkMetadataRepository, BcBLedgerNetworkDaoService> {

    @Autowired
    public BcBLedgerNetworkDaoServiceTest(BcBLedgerNetworkDaoService service, BcBLedgerNetworkMetadataRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcBLedgerNetworkMetadata getEToInsert() {
        return BcBLedgerNetworkMetadata.builder()
                .networkId("network-001")
                .contentHash("hash-test")
                .content("{\"test\":true}")
                .build();
    }

    @Override
    protected BcBLedgerNetworkMetadata editE(BcBLedgerNetworkMetadata entity) {
        entity.setContent("{\"test\":false}");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcBLedgerNetworkMetadata original, BcBLedgerNetworkMetadata edited) {
        return !original.getContent().equals(edited.getContent());
    }
}
