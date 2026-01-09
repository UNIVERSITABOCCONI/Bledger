package it.bocconi.bledger.feature.am.router;

import it.bocconi.bledger.feature.am.service.AmService;
import it.bocconi.bledger.feature.file.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AmHandler {

    private final AmService amService;

    public Mono<ServerResponse> handleUploadCompanies(ServerRequest request) {
        return request.multipartData().flatMap(multiPartData -> {
                            FilePart zip = (FilePart) multiPartData.getFirst(FileType.COMPANIES_ZIP.name());

                            if (zip == null) {
                                return ServerResponse.badRequest().bodyValue("Missing companies zip");
                            }

                            return amService.uploadCompanies(zip)
                                    .then(
                                            ServerResponse.status(201)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(Map.of("status", "imported"))
                                    );
                        });
    }
}
