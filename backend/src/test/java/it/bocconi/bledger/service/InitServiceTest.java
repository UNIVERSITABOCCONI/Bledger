package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.init.InitService;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class InitServiceTest {

    @Autowired
    private InitService initService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void importCompaniesSkipsWhenAlreadyPresent() {
        StepVerifier.create(initService.importCompanies())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deploySmartContractsReturnsExistingContracts() {
        StepVerifier.create(initService.deploySmartContracts().collectList())
                .expectNextMatches(list ->
                        list.size() == 2
                                && list.stream().anyMatch(sc -> sc.getType() == SmartContractType.IDENTITY)
                                && list.stream().anyMatch(sc -> sc.getType() == SmartContractType.ERC721)
                )
                .verifyComplete();
    }
}
