package it.bocconi.bledger.abstraction.dao.service;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class BcAbstractDaoService<E extends BcAbstractEntity, R extends BcAbstractRepository<E>> {

    protected final R repository;

    protected final R2dbcEntityTemplate template;
    private final Class<E> entityClass;
    private final DatabaseClient databaseClient;

    public BcAbstractDaoService(R repository, R2dbcEntityTemplate template){
        this.repository = repository;
        this.template = template;
        this.entityClass = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.databaseClient = template.getDatabaseClient();
    }


    @Transactional
    public Mono<E> save(E entity) {
        entity.prePersist();
        return repository.save(entity)
                .map(savedEntity -> {
                    savedEntity.markAsNotNew();
                    return savedEntity;
                });
    }

    @Transactional
    public Flux<E> saveAll(List<E> entities) {
        entities.forEach(BcAbstractEntity::prePersist);
        return repository.saveAll(entities)
                .doOnNext(BcAbstractEntity::markAsNotNew
                );
    }

    @Transactional
    public Mono<E> delete(E entity) {
        entity.preDelete();
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Mono<E> findById(String id) {
        return repository.findByIdAndDeletedFalse(id);
    }

    @Transactional(readOnly = true)
    public Mono<Boolean> existsById(String id) {
        return repository.existsByIdAndDeletedFalse(id);
    }

    @Transactional(readOnly = true)
    public Mono<Long> count() {
        return repository.countByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Flux<E> findAllByIds(List<String> ids) {
        return repository.findAllByDeletedFalseAndIdIn(ids);
    }


    @Transactional(readOnly = true)
    public Mono<Page<E>> findAllByCriteria(
            Criteria criteria,
            Pageable pageable
    ) {

        Criteria criteriaWithDeletedConstraint = criteria.and("deleted").is(false);
        Query query = Query.query(criteriaWithDeletedConstraint)
                .with(pageable);

        Mono<Long> count = template.count(Query.query(criteriaWithDeletedConstraint), entityClass);

        Flux<E> results = template.select(query, entityClass);

        return results.collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    public <T> Mono<Page<T>> findPage(
            String baseQuery,
            String countQuery,
            Map<String, Object> parameters,
            Pageable pageable,
            BiFunction<Row, RowMetadata, T> mapper
    ) {
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        String orderBy = buildOrderByClause(pageable.getSort());
        String paginatedQuery = baseQuery + orderBy + " LIMIT :limit OFFSET :offset";

        Map<String, Object> paginationParams = new HashMap<>();
        paginationParams.put("limit", pageable.getPageSize());
        paginationParams.put("offset", offset);

        Set<Map.Entry<String, Object>> entries = parameters.entrySet();
        Map<String, Object> collect = entries.stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        var specQuery = databaseClient.sql(paginatedQuery)
                .bindValues(collect)
                .bindValues(paginationParams);

        var specNumber = databaseClient.sql(countQuery)
                .bindValues(collect);

        for (Map.Entry<String, Object> entry : entries) {
            if(entry.getValue() != null) continue;
            specQuery = specQuery.bindNull(entry.getKey(), String.class);
            specNumber = specNumber.bindNull(entry.getKey(), String.class);
        }

        Mono<List<T>> contentMono = specQuery
                .map(mapper)
                .all()
                .collectList();

        Mono<Long> countMono = specNumber
                .map((row, meta) -> row.get(0, Long.class))
                .one();

        return Mono.zip(contentMono, countMono)
                .map(tuple -> {
                    List<T> content = tuple.getT1();
                    long total = tuple.getT2();
                    return new PageImpl<>(content, pageable, total);
                });
    }


    private String buildOrderByClause(Sort sort) {
        if (sort == null || sort.isUnsorted()) return "";
        return " ORDER BY " + sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(Collectors.joining(", "));
    }


}
