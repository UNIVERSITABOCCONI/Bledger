package it.bocconi.bledger.dao.service;


import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.dao.repository.BcTransactionRepository;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcTransactionDaoServiceTest extends BcAbstractDaoServiceTest<BcTransaction, BcTransactionRepository, BcTransactionDaoService> {

    @Autowired
    public BcTransactionDaoServiceTest(BcTransactionDaoService service, BcTransactionRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcTransaction getEToInsert() {
        return BcTransaction
                .builder()
                .txHash("hash")
                .creatorId("company-001")
                .fromWalletId("wallet-001")
                .fromWalletAddress("address-001")
                .toAddress("address-002")
                .gasPrice(BigInteger.ONE)
                .gasUsed(BigInteger.TEN)
                .blockNumber(BigInteger.ONE)
                .status(TransactionStatus.PENDING)
                .payload("payload")
                .transactionData("transaction-data")
                .readyAt(LocalDateTime.now())
                .tryCount(0)
                .confirmedAt(LocalDateTime.now())
                .transactionType(TransactionType.CREATE_NETWORK)
                .networkId("network-001")
                .nodeId("node-001")
                .transactionReceipt("{\"status\":\"ok\"}")
                .build();
    }

    @Override
    protected BcTransaction editE(BcTransaction entity) {
        entity.setTryCount(2);
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcTransaction original, BcTransaction edited) {
        return !original.getTryCount().equals(edited.getTryCount());
    }
}
