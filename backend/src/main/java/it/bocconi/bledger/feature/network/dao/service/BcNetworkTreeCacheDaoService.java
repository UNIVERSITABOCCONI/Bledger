package it.bocconi.bledger.feature.network.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.network.entity.BcNetworkTreeCache;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkTreeCacheRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BcNetworkTreeCacheDaoService extends BcAbstractDaoService<BcNetworkTreeCache, BcNetworkTreeCacheRepository> {
    public BcNetworkTreeCacheDaoService(BcNetworkTreeCacheRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Mono<BcNetworkTreeCache> findByNetworkId(String networkId) {
        return repository.findByNetworkIdAndDeletedFalse(networkId);
    }

    public Mono<Integer> deleteByNetworkId(String networkId) {
        return repository.deleteByNetworkIdAndDeletedFalse(networkId);
    }
}
