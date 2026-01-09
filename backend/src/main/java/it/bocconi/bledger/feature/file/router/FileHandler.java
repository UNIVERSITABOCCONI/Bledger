package it.bocconi.bledger.feature.file.router;


import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.enums.FileType;
import it.bocconi.bledger.feature.file.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class FileHandler {

    private final FileService fileService;
    public Mono<ServerResponse> handleUpload(ServerRequest request) {
        String networkId = request.pathVariable("networkId");

        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        request.multipartData().flatMap(multiPartData -> {
                            FilePart scope1 = (FilePart) multiPartData.getFirst(FileType.GRANULAR_SCOPE_1.name());
                            FilePart scope2 = (FilePart) multiPartData.getFirst(FileType.GRANULAR_SCOPE_2.name());
                            FilePart global = (FilePart) multiPartData.getFirst(FileType.GLOBAL_SCOPE_FILE.name());

                            if (scope1 == null || scope2 == null || global == null) {
                                return ServerResponse.badRequest().bodyValue("Missing one or more files");
                            }

                            return fileService.uploadFiles(networkId, Mono.just(companyId), scope1, scope2, global)
                                    .flatMap(globalJson ->
                                            ServerResponse.ok()
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(globalJson)
                                    );
                        })
                );
    }

    public Mono<ServerResponse> handleConfirmFileData(ServerRequest request) {
        String networkId = request.pathVariable("networkId");

        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        fileService.confirmFileDataAndExtractMetadata(networkId, companyId)
                                .flatMap(body -> ServerResponse.ok().bodyValue(body))
                );
    }

    public Mono<ServerResponse> handleExportFileData(ServerRequest request) {
        String networkId = request.pathVariable("networkId");

        return RequestContext.getCompanyId()
                .flatMap(companyId -> fileService.generateExportZip(networkId, companyId))
                .flatMap(zipBytes -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=\"export.zip\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .bodyValue(zipBytes)
                );
    }

    public Mono<ServerResponse> handleDeleteFileData(ServerRequest request) {
        String networkId = request.pathVariable("networkId");

        return RequestContext.getCompanyId()
                .flatMap(companyId -> fileService.deleteFileData(networkId, companyId))
                .then(ServerResponse.ok().build());
    }

    public Mono<ServerResponse> downloadFile(ServerRequest request) {
        String nodeId = request.pathVariable("nodeId");
        FileType type = FileType.valueOf(request.pathVariable("type"));

        return fileService.downloadFile(nodeId, type)
                .flatMap(tuple -> {
                    BcFile file = tuple.getT1();
                    BcFileBinary binary = tuple.getT2();
                    return ServerResponse.ok()
                            .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(file.getMimeType()))
                            .bodyValue(binary.getFileBinary());
                });
    }

    public Mono<ServerResponse> downloadAll(ServerRequest request) {
        String nodeId = request.pathVariable("nodeId");

        return fileService.downloadAll(nodeId)
                .flatMap(zip -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + nodeId + ".zip\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .bodyValue(zip));
    }
}
