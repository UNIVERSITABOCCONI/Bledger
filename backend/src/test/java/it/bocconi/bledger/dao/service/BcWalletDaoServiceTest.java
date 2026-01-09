package it.bocconi.bledger.dao.service;


import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.wallet.dao.repository.BcWalletRepository;
import it.bocconi.bledger.feature.wallet.dao.service.BcWalletDaoService;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Random;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcWalletDaoServiceTest extends BcAbstractDaoServiceTest<BcWallet, BcWalletRepository, BcWalletDaoService> {

    @Autowired
    public BcWalletDaoServiceTest(BcWalletDaoService service, BcWalletRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcWallet getEToInsert() {
        return BcWallet
                .builder()
                .walletAddress("0xABCDEF1234567890")
                .toDeploy(true)
                .deployed(false)
                .walletType(WalletType.PLATFORM)
                .balance(BigDecimal.ZERO)
                .deployedAt(LocalDateTime.now())
                .salt(BigInteger.valueOf(new Random().nextInt()))
                .isWhitelisted(true)
                .build()
                ;
    }

    @Override
    protected BcWallet editE(BcWallet entity) {
        entity.setBalance(BigDecimal.valueOf(1000));
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcWallet original, BcWallet edited) {
        return !original.getBalance().equals(edited.getBalance());
    }
}
