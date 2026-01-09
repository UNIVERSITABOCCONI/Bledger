package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.network.entity.BcNetworkTreeCache;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkTreeCacheRepository;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkTreeCacheDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcNetworkTreeCacheDaoServiceTest extends BcAbstractDaoServiceTest<BcNetworkTreeCache, BcNetworkTreeCacheRepository, BcNetworkTreeCacheDaoService> {

    @Autowired
    public BcNetworkTreeCacheDaoServiceTest(BcNetworkTreeCacheDaoService service, BcNetworkTreeCacheRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcNetworkTreeCache getEToInsert() {
        return BcNetworkTreeCache
                .builder()
                .networkId("network-001")
                .treeJson("Sample tree cache data")
                .build();
    }

    @Override
    protected BcNetworkTreeCache editE(BcNetworkTreeCache entity) {
        entity.setTreeJson("Updated tree cache data");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcNetworkTreeCache original, BcNetworkTreeCache edited) {
        return !original.getTreeJson().equals(edited.getTreeJson());
    }
}
