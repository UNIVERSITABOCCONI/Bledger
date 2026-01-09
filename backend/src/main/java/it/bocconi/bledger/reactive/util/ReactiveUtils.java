package it.bocconi.bledger.reactive.util;


import it.bocconi.bledger.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReactiveUtils {

    private ReactiveUtils() {
        // Utility class, no instantiation allowed
    }

    public static <T> Mono<T> scheduleOnBundleElasticMono(Callable<T> callable){
        return Mono.fromCallable(callable).subscribeOn(Schedulers.boundedElastic());
    }

    public static <T> Flux<T> scheduleOnBundleElasticFlux(Callable<Flux<T>> callable) {
        return Mono.fromCallable(callable)
                .flatMapMany(flux -> flux.subscribeOn(Schedulers.boundedElastic()));
    }

    public static <T> Mono<T> getBodyFromRequest(ServerRequest serverRequest, Class<T> clazz) {
        return serverRequest.bodyToMono(clazz)
                .switchIfEmpty(Mono.error(new BadRequestException("Missing or invalid body")));
    }

    public static Pageable extractPageable(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        Sort sort = request.queryParams().getOrDefault("sort", List.of()).stream()
                .map(sortStr -> {
                    String[] parts = sortStr.split(",");
                    String property = parts[0];
                    Sort.Direction direction = parts.length > 1
                            ? Sort.Direction.fromOptionalString(parts[1]).orElse(Sort.Direction.ASC)
                            : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));

        return PageRequest.of(page, size, sort);
    }

    public static Pageable addPrefixToPageable(Pageable pageable, String prefix) {
        Sort prefixedSort = Sort.by(
                pageable.getSort().stream()
                        .map(order -> new Sort.Order(order.getDirection(), prefix + order.getProperty()))
                        .toList()
        );

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), prefixedSort);
    }


}
