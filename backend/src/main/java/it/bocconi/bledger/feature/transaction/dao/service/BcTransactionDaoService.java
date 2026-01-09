package it.bocconi.bledger.feature.transaction.dao.service;


import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.dao.repository.BcTransactionRepository;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Set;

@Service
public class BcTransactionDaoService extends BcAbstractDaoService<BcTransaction, BcTransactionRepository> {

    public BcTransactionDaoService(BcTransactionRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    @Transactional(readOnly = true)
    public Flux<BcTransaction> fetchToExecute(int limit){
        List<TransactionStatus> statuses = List.of(TransactionStatus.PENDING, TransactionStatus.FAIL_PENDING);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "readyAt"));
        return repository.findByDeletedFalseAndStatusInOrderByReadyAtAsc(statuses, pageable);
    }

    public Mono<Page<BcTransaction>> findByNetworkAndTypes(String networkId, Set<TransactionType> types, Pageable pageable) {
        Criteria c = Criteria.where("network_id").is(networkId)
                .and("deleted").is(false)
                .and("transaction_type").in(types);

        return findAllByCriteria(c, pageable);
    }

    public Mono<Page<BcTransaction>> findByNodeAndTypes(String nodeId, Set<TransactionType> types, Pageable pageable) {
        Criteria c = Criteria.where("node_id").is(nodeId)
                .and("deleted").is(false)
                .and("transaction_type").in(types);
        return findAllByCriteria(c, pageable);
    }
}
