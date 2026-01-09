package it.bocconi.bledger.feature.network.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.network.entity.BcNodeMetadata;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BcNodeMetadataRepository extends BcAbstractRepository<BcNodeMetadata> {
    Mono<Integer> deleteAllByNodeId(String nodeId);
    Flux<BcNodeMetadata> findByNodeIdAndMetadataType(String id, NodeMetadataType nodeMetadataType);
}
