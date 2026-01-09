package it.bocconi.bledger.feature.file.router;

import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class FileRouter {

    private final static String FILE_PATH = "/file";
    private final static String BU_PATH = BLPaths.BU + FILE_PATH;
    private final static String COMMON_PATH = BLPaths.COMMON + FILE_PATH;

    @Bean
    public RouterFunction<ServerResponse> fileRoute(FileHandler handler, AuthFilterAppender authFilterAppender) {
        return authFilterAppender.routerFunctionWithAuthFiler(RouterFunctions
                .route(POST(BU_PATH + "/upload-scope-files/{networkId}"), handler::handleUpload)
                .andRoute(POST(BU_PATH + "/confirm-file-data/{networkId}"), handler::handleConfirmFileData)
                .andRoute(GET(BU_PATH + "/export/{networkId}"), handler::handleExportFileData)
                .andRoute(DELETE(BU_PATH + "/delete-data/{networkId}"), handler::handleDeleteFileData)
                .andRoute(GET(COMMON_PATH + "/download/{nodeId}/{type}"), handler::downloadFile)
                .andRoute(GET(COMMON_PATH + "/download/{nodeId}"), handler::downloadAll)
        );
    }
}
