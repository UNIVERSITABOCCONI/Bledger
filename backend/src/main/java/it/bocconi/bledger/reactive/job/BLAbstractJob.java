package it.bocconi.bledger.reactive.job;

import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;

@AllArgsConstructor
public abstract class BLAbstractJob implements BLJob{

    private final Environment environment;

    @Override
    public String getCronExpression() {
        String classInSnakeCase = this.getClass().getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

        return environment.getProperty("it.bocconi.bledger.job." + classInSnakeCase + ".cron",
                "0 0 * * * ?"); // Default to every hour if not specified
    }
}
