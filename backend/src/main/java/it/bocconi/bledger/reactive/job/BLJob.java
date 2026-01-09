package it.bocconi.bledger.reactive.job;

import reactor.core.publisher.Mono;

public interface BLJob {

    Mono<Void> execute();
    String getCronExpression();
}
