package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import it.bocconi.bledger.feature.network.dao.repository.BcNodeRepository;
import it.bocconi.bledger.feature.network.dao.service.BcNodeDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcNodeDaoServiceTest extends BcAbstractDaoServiceTest<BcNode, BcNodeRepository, BcNodeDaoService> {

    @Autowired
    public BcNodeDaoServiceTest(BcNodeDaoService service, BcNodeRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcNode getEToInsert() {
        return BcNode.builder()
                .networkId("network-001")
                .parentId("node-001")
                .companyId("company-001")
                .scopeFileId("file-001")
                .granularScope1FileId("file-002")
                .granularScope2FileId("file-001")
                .nodeStatus(NodeStatus.ACCEPTED)
                .audited(true)
                .auditedAt(LocalDateTime.now())
                .uploadFileTransactionId("tx-001")
                .scope3TransactionId("tx-002")
                .auditFileTransactionId("tx-001")
                .scope1("10")
                .scope2("20")
                .scope3(BigDecimal.valueOf(30.5))
                .eValue(BigDecimal.valueOf(12.3))
                .productionVolume(BigDecimal.valueOf(100.0))
                .quantity(BigDecimal.valueOf(250.0))
                .transportationEmission(BigDecimal.valueOf(5.5))
                .totalScope1AndScope2("30")
                .nodeDepth(5)
                .approvalExpirationDate(LocalDate.now().plusDays(30))
                .auditorId("company-002")
                .lastExport(LocalDateTime.now())
                .lastCompute(LocalDateTime.now())
                .lastComputeChange(LocalDateTime.now())
                .build();
    }

    @Override
    protected BcNode editE(BcNode entity) {
        entity.setCompanyId("company-002");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcNode original, BcNode edited) {
        return !original.getCompanyId().equals(edited.getCompanyId());
    }
}
