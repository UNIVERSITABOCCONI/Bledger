package it.bocconi.bledger.abstraction.dao.repository;

import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BcAbstractRepository<E extends BcAbstractEntity> extends ReactiveCrudRepository<E, String> {

    Mono<E> findByIdAndDeletedFalse(String id);

    Mono<Boolean> existsByIdAndDeletedFalse(String id);

    Mono<Long> countByDeletedFalse();

    Flux<E> findAllByDeletedFalseAndIdIn(List<String> ids);
}
