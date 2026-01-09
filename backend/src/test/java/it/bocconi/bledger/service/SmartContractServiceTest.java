package it.bocconi.bledger.service;

import it.bocconi.bledger.exception.BadRequestException;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.smartcontract.service.SmartContractService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class SmartContractServiceTest {

    @Autowired
    private SmartContractService smartContractService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void deployIdentityFailsWhenAlreadyDeployed() {
        StepVerifier.create(smartContractService.deploySmartContract(SmartContractType.IDENTITY))
                .expectError(BadRequestException.class)
                .verify();
    }
}
