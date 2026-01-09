package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BcTransactionDaoService transactionDaoService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void createTransactionPersistsPendingRecord() {
        StepVerifier.create(
                        transactionService.createTransaction(
                                        TransactionType.UPLOAD_FILE,
                                        "company-001",
                                        "0xRecipient",
                                        "{}",
                                        "network-001",
                                        "node-001")
                                .flatMap(tx -> transactionDaoService.findById(tx.getId()))
                )
                .expectNextMatches(tx ->
                        tx.getStatus() == TransactionStatus.PENDING
                                && tx.getTransactionType() == TransactionType.UPLOAD_FILE
                                && "company-001".equals(tx.getCreatorId())
                )
                .verifyComplete();
    }

    @Test
    void markTransactionAsProcessedUpdatesStatusAndConfirmedAt() {
        StepVerifier.create(transactionService.markTransactionAsProcessed("tx-002"))
                .expectNextMatches(tx -> tx.getStatus() == TransactionStatus.CONFIRMED && tx.getConfirmedAt() != null)
                .verifyComplete();
    }

    @Test
    void markTransactionAsFailedIncrementsTryCount() {
        StepVerifier.create(
                        transactionService.createTransaction(
                                        TransactionType.AUDIT,
                                        "company-001",
                                        "0xRecipient",
                                        "{}",
                                        "network-001",
                                        "node-001")
                                .flatMap(tx -> transactionService.markTransactionAsFailed(tx.getId()))
                                .flatMap(tx -> transactionDaoService.findById(tx.getId()))
                )
                .expectNextMatches(tx ->
                        tx.getStatus() == TransactionStatus.FAIL_PENDING
                                && Integer.valueOf(1).equals(tx.getTryCount())
                )
                .verifyComplete();
    }
}
