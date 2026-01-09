package it.bocconi.bledger.feature.init;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@AllArgsConstructor
@Slf4j
@Profile("!unit-test")
public class ApplicationReadyListener {

    private final InitService initService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        deploySmartContracts()
                .then(importCompanies())
                .subscribe();
    }

    private Mono<Void> importCompanies() {
        return initService.importCompanies()
                .doOnSuccess(result -> {
                    if (result) {
                        log.info("Companies imported successfully.");
                    } else {
                        log.warn("No companies were imported.");
                    }
                })
                .doOnError(error -> log.error("Error importing companies", error))
                .then();
    }

    private Mono<Void> deploySmartContracts(){
        return initService.deploySmartContracts()
                .doOnSubscribe(s -> log.info("Starting smart contract deployment..."))
                .doOnNext(sc ->
                        log.info("Contract type {} address is {}", sc.getType(), sc.getContractAddress())
                )
                .doOnError(err ->
                        log.error("Deployment stream failed", err)
                )
                .doOnComplete(() ->
                        log.info("All smart contracts processed.")
                )
                .collectList()
                .then();
    }
}
