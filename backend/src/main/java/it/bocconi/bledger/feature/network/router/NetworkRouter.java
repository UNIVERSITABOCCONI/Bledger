package it.bocconi.bledger.feature.network.router;

import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class NetworkRouter {

    private final static String NETWORK_PATH = "/network";
    private final static String BU_PATH = BLPaths.BU + NETWORK_PATH;
    private final static String BNA_PATH = BLPaths.BNA + NETWORK_PATH;
    private final static String TPA_PATH = BLPaths.TPA + NETWORK_PATH;
    private final static String COMMON_PATH = BLPaths.COMMON + NETWORK_PATH;
    private final static String PUBLIC_PATH = BLPaths.PUBLIC + NETWORK_PATH;


    @Bean
    public RouterFunction<ServerResponse> networkRoute(NetworkHandler handler, AuthFilterAppender authFilterAppender) {
        return authFilterAppender.routerFunctionWithAuthFiler(
                RouterFunctions
                        //BU-APIS
                        .route(GET(BU_PATH + "/my-networks"), handler::getMyNetworks_bu)
                        .andRoute(GET(BU_PATH + "/details/{networkId}"), handler::getNetworkDetails)
                        .andRoute(GET(BU_PATH + "/my-invitations"), handler::getMyInvitations)
                        .andRoute(POST(BU_PATH + "/accept-invitation/{networkId}"), handler::acceptInvitation)
                        .andRoute(POST(BU_PATH + "/refuse-invitation/{networkId}"), handler::refuseInvitation)
                        .andRoute(GET(BU_PATH + "/get-auditors/{networkId}"), handler::getAuditors)
                        .andRoute(POST(BU_PATH + "/request-audit/{networkId}"), handler::requestAudit)
                        .andRoute(GET(BU_PATH + "/get-clients/{networkId}"), handler::getClients)
                        .andRoute(GET(BU_PATH + "/get-suppliers/{networkId}"), handler::getSuppliers)
                        .andRoute(GET(BU_PATH + "/network-tree/get/{networkId}"), handler::getNetworkTree_bu)
                        .andRoute(POST(BU_PATH + "/request-verification/{networkId}"), handler::requestVerification)
                        .andRoute(POST(BU_PATH + "/compute-e/{networkId}"), handler::computeEResult)
                        .andRoute(POST(BU_PATH + "/update-node/{nodeId}"), handler::updateNode)
                        .andRoute(POST(BU_PATH + "/compute-scope3/{networkId}"), handler::computeScope3)

                        //BNA-APIS
                        .andRoute(GET(BNA_PATH + "/my-networks"), handler::getMyNetworks_bna)
                        .andRoute(POST(BNA_PATH + "/create"), handler::create)
                        .andRoute(PUT(BNA_PATH + "/add-auditors/{networkId}"), handler::addAuditors)
                        .andRoute(GET(BNA_PATH + "/network-tree/get/{networkId}"), handler::getNetworkTree_bna)
                        .andRoute(PUT(BNA_PATH + "/network-tree/update/{networkId}"), handler::updateNetworkTree)
                        .andRoute(GET(BNA_PATH + "/get-other-members/{networkId}"), handler::getOtherMembers_bna)
                        .andRoute(GET(BNA_PATH + "/get-refused/{networkId}"), handler::getRefused)
                        .andRoute(GET(BNA_PATH + "/check-name-existence/{name}"), handler::checkNameExistence)


                        //TPA-APIS
                        .andRoute(GET(TPA_PATH + "/my-networks"), handler::getMyNetworks_tpa)
                        .andRoute(GET(TPA_PATH + "/my-requests/{networkId}"), handler::getMyRequests)
                        .andRoute(GET(TPA_PATH + "/get-other-members/{networkId}"), handler::getOtherMembers_tpa)
                        .andRoute(POST(TPA_PATH + "/verify-data/{nodeId}"), handler::verifyData_tpa)
                        .andRoute(GET(TPA_PATH + "/details/{networkId}"), handler::getNetworkDetails_tpa)

                        //COMMON-APIS
                        .andRoute(GET(COMMON_PATH + "/node-details/{nodeId}"), handler::getNodeDetails)

                        //PUBLIC-APIS
                        .andRoute(GET(PUBLIC_PATH + "/metadata/{metadataHash}"), handler::getMetadata)
                        .andRoute(POST(PUBLIC_PATH + "/merkle-hash"), handler::computeMerkleHash)

        );



        //       ;
    }


}
