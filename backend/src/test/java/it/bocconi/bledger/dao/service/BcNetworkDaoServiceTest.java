package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.network.entity.BcNetwork;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkRepository;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcNetworkDaoServiceTest extends BcAbstractDaoServiceTest<BcNetwork, BcNetworkRepository, BcNetworkDaoService> {

    @Autowired
    public BcNetworkDaoServiceTest(BcNetworkDaoService service, BcNetworkRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcNetwork getEToInsert() {
        return BcNetwork
                .builder()
                .rootId("node-001")
                .networkAdminId("company-001")
                .networkName("Test Network")
                .membersCount(0L)
                .uploadedCount(0L)
                .auditedCount(0L)
                .tokenId(BigInteger.valueOf(10001))
                .build();
    }

    @Override
    protected BcNetwork editE(BcNetwork entity) {
        entity.setRootId("node-002");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcNetwork original, BcNetwork edited) {
        return !original.getRootId().equals(edited.getRootId());
    }
}
