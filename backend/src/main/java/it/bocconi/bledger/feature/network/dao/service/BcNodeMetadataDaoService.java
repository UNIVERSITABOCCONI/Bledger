package it.bocconi.bledger.feature.network.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.network.entity.BcNodeMetadata;
import it.bocconi.bledger.feature.network.dao.repository.BcNodeMetadataRepository;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BcNodeMetadataDaoService extends BcAbstractDaoService<BcNodeMetadata, BcNodeMetadataRepository> {

    public BcNodeMetadataDaoService(BcNodeMetadataRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Mono<Integer> deleteAllByNodeId(String nodeId) {
        return repository.deleteAllByNodeId(nodeId);
    }

    public Flux<BcNodeMetadata> findByNodeIdAndType(String id, NodeMetadataType nodeMetadataType) {
        return repository.findByNodeIdAndMetadataType(id, nodeMetadataType);
    }
}
