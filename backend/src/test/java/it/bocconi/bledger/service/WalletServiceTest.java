package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void getOrCreateAdminWalletReturnsExistingPlatformWallet() {
        StepVerifier.create(walletService.getOrCreateAdminWallet())
                .expectNextMatches(wallet ->
                        "wallet-003".equals(wallet.getId())
                                && wallet.getWalletType() == WalletType.PLATFORM
                )
                .verifyComplete();
    }
}
