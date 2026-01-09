package it.bocconi.bledger.feature.auth.filter;

import it.bocconi.bledger.feature.auth.enums.BLRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RequestContext {
    public static final String COMPANY_ID_KEY = "companyId";
    public static final String ROLES_KEY = "roles";

    public static Mono<String> getCompanyId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(COMPANY_ID_KEY)) {
                return Mono.just(ctx.get(COMPANY_ID_KEY));
            } else {
                return Mono.error(new IllegalStateException("Company ID not found in context"));
            }
        });
    }

    @Deprecated(forRemoval = true)
    public static Flux<BLRole> getRoles() {
        return Flux.deferContextual(ctx -> {
            if (ctx.hasKey(ROLES_KEY)) {
                return Flux.fromIterable(ctx.get(ROLES_KEY));
            } else {
                return Flux.error(new IllegalStateException("Roles not found in context"));
            }
        });
    }
}
