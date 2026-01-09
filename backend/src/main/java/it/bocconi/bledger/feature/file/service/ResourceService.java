package it.bocconi.bledger.feature.file.service;


import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ResourceService {

    private static final String DEFAULT_COMPANIES_FILE = "classpath:defaults/%s/company/companies.xlsx";
    private static final String DEFAULT_COMPANIES_LOGOS_FOLDER = "classpath:defaults/%s/company/logos/**";
    private static final String DEFAULT_METADATA_JSON = "classpath:defaults/%s/xbrl-metadata/metadata.json";

    private final Environment environment;

    private final ResourcePatternResolver resolver;

    public Flux<Resource> getCompanyLogos(){
        String companyEnv = environment.getProperty("it.bocconi.bledger.defaults", "dev");
        return getResources(DEFAULT_COMPANIES_LOGOS_FOLDER.formatted(companyEnv));
    }

    public Mono<Resource> getCompaniesFile() {
        String companyEnv = environment.getProperty("it.bocconi.bledger.defaults", "dev");
        return getResource(DEFAULT_COMPANIES_FILE.formatted(companyEnv));
    }

    public Mono<Resource> getMetadataJson() {
        String companyEnv = environment.getProperty("it.bocconi.bledger.defaults", "dev");
        return getResource(DEFAULT_METADATA_JSON.formatted(companyEnv));
    }

    private Mono<Resource> getResource(String path) {
        return Mono.fromCallable(() -> resolver.getResource(path));
    }

    private Flux<Resource> getResources(String path) {
        return Mono.fromCallable(() -> resolver.getResources(path))
                .flatMapMany(Flux::fromArray)
                .filter(Resource::exists);
    }


}
