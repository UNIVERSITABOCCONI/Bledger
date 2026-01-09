package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.network.dao.service.BcNetworkTreeCacheDaoService;
import it.bocconi.bledger.feature.network.entity.BcNetworkTreeCache;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcBLedgerNetworkDaoService;
import it.bocconi.bledger.feature.smartcontract.service.BLedgerNetworkService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class BLedgerNetworkServiceTest {

    @Autowired
    private BLedgerNetworkService bLedgerNetworkService;

    @Autowired
    private BcBLedgerNetworkDaoService bcBLedgerNetworkDaoService;

    @Autowired
    private BcNetworkTreeCacheDaoService bcNetworkTreeCacheDaoService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void createBcBLedgerMetadataPersistsRecord() {
        String treeJson = """
                {
                  "id": "node-001",
                  "networkId": "network-001",
                  "companyId": "company-001",
                  "companyName": "Alpha Corp",
                  "nodeDepth": 0,
                  "children": []
                }
                """;

        StepVerifier.create(
                        bcNetworkTreeCacheDaoService.deleteByNetworkId("network-001")
                                .then(bcNetworkTreeCacheDaoService.save(
                                        BcNetworkTreeCache.builder()
                                                .networkId("network-001")
                                                .treeJson(treeJson)
                                                .build()))
                                .then(bLedgerNetworkService.createBcBLedgerMetadata("network-001"))
                                .flatMap(meta -> bcBLedgerNetworkDaoService.findById(meta.getId())
                                        .map(found -> found.getId().equals(meta.getId())))
                )
                .expectNext(true)
                .verifyComplete();
    }
}
