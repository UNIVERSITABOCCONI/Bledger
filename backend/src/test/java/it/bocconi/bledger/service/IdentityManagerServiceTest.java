package it.bocconi.bledger.service;

import it.bocconi.bledger.blockchain.service.BlockChainService;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.wallet.dao.service.BcWalletDaoService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import it.bocconi.bledger.blockchain.service.IdentityManagerService;
import it.bocconi.bledger.blockchain.util.Web3Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class IdentityManagerServiceTest {

    @Autowired
    private IdentityManagerService identityManagerService;

    @Autowired
    private BcTransactionDaoService transactionDaoService;

    @Autowired
    private BcWalletDaoService walletDaoService;

    @MockBean
    private BlockChainService blockChainService;

    @MockBean
    private Web3Utils web3Utils;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void whitelistWalletByTransactionMarksWalletWhenBlockchainFails() {
        when(blockChainService.getIdentityManager(any(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        BcTransaction tx = BcTransaction.builder()
                .transactionType(TransactionType.WHITELIST_WALLET)
                .status(TransactionStatus.PENDING)
                .payload("{\"walletId\":\"wallet-002\",\"isAuditor\":false}")
                .creatorId("company-001")
                .readyAt(LocalDateTime.now())
                .build();

        StepVerifier.create(transactionDaoService.save(tx)
                        .flatMap(saved -> identityManagerService.whitelistWalletByTransaction(saved.getId()))
                        .flatMap(result -> walletDaoService.findById("wallet-002")
                                .map(wallet -> !result && !wallet.getIsWhitelisted())))
                .expectNext(true)
                .verifyComplete();
    }
}
