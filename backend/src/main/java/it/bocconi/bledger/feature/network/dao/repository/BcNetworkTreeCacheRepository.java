package it.bocconi.bledger.feature.network.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.network.entity.BcNetworkTreeCache;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface BcNetworkTreeCacheRepository extends BcAbstractRepository<BcNetworkTreeCache> {
    Mono<BcNetworkTreeCache> findByNetworkIdAndDeletedFalse(String networkId);

    @Modifying
    @Query("""
        UPDATE bc_network_tree_cache
        SET deleted = true,
            deleted_at = NOW(),
            updated_at = NOW()
        WHERE network_id = :networkId
          AND deleted = false
    """)
    Mono<Integer> deleteByNetworkIdAndDeletedFalse(String networkId);

}
