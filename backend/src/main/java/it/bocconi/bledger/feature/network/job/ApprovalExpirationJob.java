package it.bocconi.bledger.feature.network.job;


import it.bocconi.bledger.feature.network.dao.service.BcNodeDaoService;
import it.bocconi.bledger.reactive.job.BLAbstractJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@Slf4j
public class ApprovalExpirationJob extends BLAbstractJob {

    @Value("${it.bocconi.bledger.job.approval-expiration.approval-expiration-days:3}")
    private long expirationDays;

    private final BcNodeDaoService bcNodeDaoService;
    public ApprovalExpirationJob(Environment environment, BcNodeDaoService bcNodeDaoService) {
        super(environment);
        this.bcNodeDaoService = bcNodeDaoService;
    }

    @Override
    @Transactional
    public Mono<Void> execute() {
        LocalDate childExpiry = LocalDate.now().plusDays(expirationDays);
        return bcNodeDaoService.automaticAcceptAfterExpiration(LocalDate.now(), childExpiry)
                .doOnNext(pair -> {
                    int approvedNodes = pair.getFirst();
                    int childNodes = pair.getSecond();

                    log.info(
                            "Approved {} parent nodes automatically upon expiration. {} child nodes may accept invitations until {}; after that, they will be approved automatically.",
                            approvedNodes,
                            childNodes,
                            childExpiry
                    );

                })
                .then();
    }

}
