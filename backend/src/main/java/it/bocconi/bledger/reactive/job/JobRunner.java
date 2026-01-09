package it.bocconi.bledger.reactive.job;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!unit-test")
public class JobRunner {

    public enum RunPolicy {
        /** Default: serialize runs; if a run is still executing, next ticks queue. */
        SERIALIZED,
        /** At-most-once per window: if a run is already executing, drop incoming ticks. */
        AT_MOST_ONCE_DROP_WHILE_RUNNING
    }

    private final ApplicationContext context;
    private final CronFlux cronFlux;

    private final List<Disposable> subscriptions = new CopyOnWriteArrayList<>();

    private final RunPolicy defaultPolicy = RunPolicy.AT_MOST_ONCE_DROP_WHILE_RUNNING;

    @PostConstruct
    public void startJobs() {
        log.info("Starting job runner…");

        for (String beanName : context.getBeanNamesForType(BLJob.class)) {
            BLJob job = context.getBean(beanName, BLJob.class);

            cronFlux.validate(job.getCronExpression(), beanName);

            Flux<ZonedDateTime> ticks = cronFlux.ticks(job.getCronExpression(), beanName);

            Disposable d = buildPerJobPipeline(job, beanName, ticks, defaultPolicy)
                    .subscribe(
                            ts  -> log.debug("Job '{}' completed at {}", beanName, ts),
                            err -> log.error("Unexpected error in cron pipeline for job '{}'", beanName, err)
                    );

            subscriptions.add(d);
            log.info("Scheduled job: {}", beanName);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down job runner…");
        // Dispose all per-job subscriptions to stop scheduling/running
        subscriptions.forEach(Disposable::dispose);
        subscriptions.clear();
        log.info("Job runner shut down.");
    }

    private Flux<ZonedDateTime> buildPerJobPipeline(
            BLJob job,
            String jobName,
            Flux<ZonedDateTime> ticks,
            RunPolicy policy
    ) {
        return switch (policy) {
            case AT_MOST_ONCE_DROP_WHILE_RUNNING -> {
                AtomicBoolean running = new AtomicBoolean(false);
                yield ticks.flatMap(tick -> {
                    if (!running.compareAndSet(false, true)) {
                        log.debug("Dropping tick for job '{}' at {} (previous run still active)", jobName, tick);
                        return Mono.empty();
                    }
                    Instant start = Instant.now();
                    log.debug("Job '{}' starting at {}", jobName, tick);
                    return job.execute()
                            .doOnError(e -> log.error("Job '{}' failed at {}. Will continue scheduling.",
                                    jobName, tick, e))
                            .onErrorResume(e -> Mono.empty())
                            .doFinally(sig -> {
                                Duration dur = Duration.between(start, Instant.now());
                                log.debug("Job '{}' finished (signal: {}) in {} ms",
                                        jobName, sig, dur.toMillis());
                                running.set(false);
                            })
                            .thenReturn(tick);
                });
                // Gate that prevents overlapping & drops ticks during an active run
            }
            default ->
                // Serialize runs per job; ticks will queue if a run is in progress
                    ticks.concatMap(tick -> {
                        Instant start = Instant.now();
                        log.info("Job '{}' starting at {}", jobName, tick);
                        return job.execute()
                                .doOnError(e -> log.error("Job '{}' failed at {}. Will continue scheduling.",
                                        jobName, tick, e))
                                .onErrorResume(e -> Mono.empty())
                                .doFinally(sig -> {
                                    Duration dur = Duration.between(start, Instant.now());
                                    log.info("Job '{}' finished (signal: {}) in {} ms",
                                            jobName, sig, dur.toMillis());
                                })
                                .thenReturn(tick);
                    });
        };
    }
}
