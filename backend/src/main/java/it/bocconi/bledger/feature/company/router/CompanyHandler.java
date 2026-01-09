package it.bocconi.bledger.feature.company.router;


import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.company.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class CompanyHandler {

    private final CompanyService companyService;

    public Mono<ServerResponse> getCompaniesByType(ServerRequest request) {
        return companyService
                .getCompaniesByType(CompanyType.valueOf(request.pathVariable("type")))
                .collectList()
                .flatMap(companyLightDto -> ServerResponse.ok()
                        .bodyValue(companyLightDto));
    }

    public Mono<ServerResponse> getMyCompany(ServerRequest request) {
        return companyService.getMyCompany()
                .flatMap(companyDto -> ServerResponse.ok()
                        .bodyValue(companyDto));
    }

    public Mono<ServerResponse> getCompanyProfileImage(ServerRequest request) {
        return companyService.getCompanyProfileImage()
                .flatMap(companyProfileImageDto ->
                        ServerResponse.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", companyProfileImageDto.filename()))
                                .contentType(MediaType.parseMediaType(companyProfileImageDto.mimeType()))
                                .contentLength(companyProfileImageDto.size())
                                .bodyValue(new ByteArrayResource(companyProfileImageDto.fileBinary())))
                .doOnError(error -> log.error("Error retrieving company profile image", error));
    }
}
