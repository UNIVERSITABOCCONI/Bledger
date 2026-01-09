package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.network.entity.BcNodeMetadata;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import it.bocconi.bledger.feature.network.dao.repository.BcNodeMetadataRepository;
import it.bocconi.bledger.feature.network.dao.service.BcNodeMetadataDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcNodeMetadataDaoServiceTest extends BcAbstractDaoServiceTest<BcNodeMetadata, BcNodeMetadataRepository, BcNodeMetadataDaoService> {

    @Autowired
    public BcNodeMetadataDaoServiceTest(BcNodeMetadataDaoService service, BcNodeMetadataRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcNodeMetadata getEToInsert() {
        return BcNodeMetadata
                .builder()
                .nodeId("node-001")
                .metadataType(NodeMetadataType.GLOBAL_SCOPE)
                .metadataKey("test-key")
                .metadataValue("test-value")
                .category("scope")
                .sourceOfEmission("energy")
                .unitOfMeasure("kWh")
                .build();
    }

    @Override
    protected BcNodeMetadata editE(BcNodeMetadata entity) {
        entity.setMetadataValue("edited-value");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcNodeMetadata original, BcNodeMetadata edited) {
        return !original.getMetadataValue().equals(edited.getMetadataValue());
    }
}
