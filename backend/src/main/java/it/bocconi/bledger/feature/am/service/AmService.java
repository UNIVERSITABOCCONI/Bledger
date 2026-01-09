package it.bocconi.bledger.feature.am.service;

import it.bocconi.bledger.feature.company.service.CompanyService;
import it.bocconi.bledger.feature.file.util.FileUtils;
import it.bocconi.bledger.feature.file.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AmService {

    private final CompanyService companyService;

    public Mono<Void> uploadCompanies(FilePart zip) {
        return FileUtils.getBytesFromFilePartMono(zip)
                .flatMap(ZipUtil::readCompaniesZipMono)
                .flatMap(companiesZip ->
                        Flux.fromIterable(companiesZip.logos())
                                .flatMap(logoEntry -> FileUtils.processLogoMono(logoEntry.fileName(), logoEntry.bytes()))
                                .collectList()
                                .flatMap(logoList -> {
                                    Resource companiesResource = new ByteArrayResource(companiesZip.companiesExcel());
                                    return companyService
                                            .importCompaniesFromLogosAndExcel(logoList, companiesResource)
                                            .then();
                                })
                );
    }
}
