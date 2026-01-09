package it.bocconi.bledger.feature.transaction.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface BcTransactionRepository extends BcAbstractRepository<BcTransaction> {

    Flux<BcTransaction> findByDeletedFalseAndStatusInOrderByReadyAtAsc(
            Collection<TransactionStatus> statuses, Pageable pageable
    );
}
