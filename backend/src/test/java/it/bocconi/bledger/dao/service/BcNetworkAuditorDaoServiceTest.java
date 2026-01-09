package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.network.entity.BcNetworkAuditor;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkAuditorRepository;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkAuditorDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcNetworkAuditorDaoServiceTest extends BcAbstractDaoServiceTest<BcNetworkAuditor, BcNetworkAuditorRepository, BcNetworkAuditorDaoService> {

    @Autowired
    public BcNetworkAuditorDaoServiceTest(BcNetworkAuditorDaoService service, BcNetworkAuditorRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcNetworkAuditor getEToInsert() {
        return BcNetworkAuditor
                .builder()
                .auditorId("company-001")
                .networkId("network-001")
                .requestsCount(2L)
                .auditedCount(1L)
                .build();
    }

    @Override
    protected BcNetworkAuditor editE(BcNetworkAuditor entity) {
        entity.setAuditorId("company-002");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcNetworkAuditor original, BcNetworkAuditor edited) {
        return !original.getAuditorId().equals(edited.getAuditorId());
    }
}
