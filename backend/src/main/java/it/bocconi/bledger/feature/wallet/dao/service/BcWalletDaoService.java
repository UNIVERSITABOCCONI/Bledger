package it.bocconi.bledger.feature.wallet.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.wallet.dao.repository.BcWalletRepository;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BcWalletDaoService extends BcAbstractDaoService<BcWallet, BcWalletRepository> {
    public BcWalletDaoService(BcWalletRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }
    public Mono<BcWallet> findByType(WalletType type) {
        return repository.findByWalletType(type);
    }
}
