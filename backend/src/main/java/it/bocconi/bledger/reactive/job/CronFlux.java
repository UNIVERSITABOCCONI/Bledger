package it.bocconi.bledger.reactive.job;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class CronFlux {
    private final CronParser parser =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

    public void validate(String cronExpression, String jobName) {
        try {
            Cron cron = parser.parse(cronExpression);
            ExecutionTime exec = ExecutionTime.forCron(cron);
            exec.nextExecution(ZonedDateTime.now()).orElseThrow(() ->
                    new IllegalArgumentException("No future execution for cron: " + cronExpression));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid cron for job '" + jobName + "': " + cronExpression, e);
        }
    }

    public Flux<ZonedDateTime> ticks(String cronExpression, String jobName) {
        Cron cron = parser.parse(cronExpression);
        ExecutionTime exec = ExecutionTime.forCron(cron);

        return Flux.defer(() -> Mono.fromCallable(() -> {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime next = exec.nextExecution(now)
                            .orElseThrow(() -> new IllegalStateException(
                                    "No next execution found for cron expression: " + cronExpression));
                    log.debug("Next execution for job '{}' is at: {}", jobName, next);
                    Duration wait = Duration.between(ZonedDateTime.now(), next);
                    if (wait.isNegative()) wait = Duration.ZERO;
                    return wait;
                }))
                .flatMap(wait -> Mono.delay(wait).thenReturn(ZonedDateTime.now()))
                .repeat();
    }
}
