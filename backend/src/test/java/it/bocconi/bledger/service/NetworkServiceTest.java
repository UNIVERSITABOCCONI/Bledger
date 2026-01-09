package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.network.service.NetworkService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class NetworkServiceTest {

    @Autowired
    private NetworkService networkService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void getMyNetworksReturnsAdminNetworks() {
        StepVerifier.create(networkService.getMyNetworks_bna(PageRequest.of(0, 10))
                        .contextWrite(Context.of(RequestContext.COMPANY_ID_KEY, "company-001")))
                .expectNextMatches(page ->
                        page.getContent().stream().anyMatch(row -> "network-001".equals(row.getId()))
                )
                .verifyComplete();
    }

    @Test
    void getNetworkDetailsTpaLoadsSmartContractInfo() {
        StepVerifier.create(networkService.getNetworkDetails_tpa("network-001")
                        .contextWrite(Context.of(RequestContext.COMPANY_ID_KEY, "company-002")))
                .expectNextMatches(dto ->
                        "network-001".equals(dto.id())
                                && "Sustainability Network".equals(dto.networkName())
                )
                .verifyComplete();
    }

    @Test
    void getMetadataReturnsStoredContent() {
        StepVerifier.create(networkService.getMetadata("hash-001"))
                .expectNext("{\"network\":\"data\"}")
                .verifyComplete();
    }
}
