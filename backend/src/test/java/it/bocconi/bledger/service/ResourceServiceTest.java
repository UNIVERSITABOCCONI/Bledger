package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.file.service.ResourceService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class ResourceServiceTest {

    @Autowired
    private ResourceService resourceService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void getCompaniesFileReturnsResource() {
        StepVerifier.create(resourceService.getCompaniesFile())
                .expectNextMatches(resource -> resource.exists() && resource.getFilename() != null)
                .verifyComplete();
    }

    @Test
    void getCompanyLogosReturnsEntries() {
        StepVerifier.create(resourceService.getCompanyLogos().collectList())
                .expectNextMatches(list -> !list.isEmpty())
                .verifyComplete();
    }
}
