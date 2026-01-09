package it.bocconi.bledger.abstraction;

import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@Slf4j
public abstract class BcAbstractDaoServiceTest<E extends BcAbstractEntity, R extends BcAbstractRepository<E>, S extends BcAbstractDaoService<E, R>> {

    protected final S service;
    protected final R repository;

    protected BcAbstractDaoServiceTest(S service, R repository) {
        this.service = service;
        this.repository = repository;
    }

    protected abstract E getEToInsert();

    protected abstract E editE(E entity);

    protected abstract boolean isEditEffective(E original, E edited);

    @Test
    protected void testSave() {
        E entity = getEToInsert();
        service.save(entity)
                .flatMap((savedEntity) -> service.findById(savedEntity.getId()))
                .as(StepVerifier::create)
                .expectNextMatches(foundEntity -> {
                    boolean notNull = foundEntity != null;
                    log.info("Entity found: {}", notNull);
                    if(!notNull) {
                        return false;
                    }

                    boolean idMatches = foundEntity.getId().equals(entity.getId());
                    log.info("Entity ID matches: {}", idMatches);
                    if(!idMatches) {
                        return false;
                    }

                    boolean createdAtNotNull = foundEntity.getCreatedAt() != null;
                    log.info("Entity createdAt not null: {}", createdAtNotNull);

                    boolean updatedAtNotNull = foundEntity.getUpdatedAt() != null;
                    log.info("Entity updatedAt not null: {}", updatedAtNotNull);

                    return createdAtNotNull && updatedAtNotNull;
                    })
                .verifyComplete();
    }

    @Test
    protected void testUpdate() {
        E entity = getEToInsert();
        service.save(entity)
                .flatMap((savedEntity) -> service.findById(savedEntity.getId()))
                .flatMap(original -> {
                    E edited = editE(original);
                    return service.save(edited);
                })
                .flatMap((savedEntity) -> service.findById(savedEntity.getId()))
                .as(StepVerifier::create)
                .expectNextMatches(edited -> {
                    boolean notNull = edited != null;
                    log.info("Edited entity found: {}", notNull);
                    if(!notNull) {
                        return false;
                    }

                    boolean isEditEffective = isEditEffective(entity, edited);
                    log.info("Edit is effective: {}", isEditEffective);
                    if(!isEditEffective) {
                        return false;
                    }

                    boolean differentTimestamps = !edited.getCreatedAt().equals(edited.getUpdatedAt());

                    log.info("CreatedAt and UpdatedAt are different: {}", differentTimestamps);

                    return differentTimestamps;

                })
                .verifyComplete();
    }

    @Test
    protected void testDelete() {
        E entity = getEToInsert();
        service.save(entity)
                .flatMap((savedEntity) -> service.findById(savedEntity.getId()))
                .flatMap(service::delete)
                .then(service.findById(entity.getId()))
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
        repository.findById(entity.getId())
                .as(StepVerifier::create)
                .expectNextMatches(foundEntity -> foundEntity != null && foundEntity.getDeleted() && foundEntity.getDeletedAt() != null)
                .verifyComplete();
    }

}
