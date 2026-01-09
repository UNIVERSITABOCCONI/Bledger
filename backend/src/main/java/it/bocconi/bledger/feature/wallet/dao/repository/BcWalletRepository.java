package it.bocconi.bledger.feature.wallet.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import reactor.core.publisher.Mono;

public interface BcWalletRepository extends BcAbstractRepository<BcWallet> {
    Mono<BcWallet> findByWalletType(WalletType type);
}
