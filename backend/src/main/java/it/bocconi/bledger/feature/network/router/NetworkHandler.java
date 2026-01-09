package it.bocconi.bledger.feature.network.router;


import it.bocconi.bledger.feature.network.router.dto.*;
import it.bocconi.bledger.feature.network.service.NetworkService;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import it.bocconi.bledger.feature.smartcontract.utils.TreeHasher;

import java.util.Map;

import static it.bocconi.bledger.reactive.util.ReactiveUtils.getBodyFromRequest;

@Component
@AllArgsConstructor
@Slf4j
public class NetworkHandler {

    private final NetworkService networkService;

    public Mono<ServerResponse> getMyNetworks_bna(ServerRequest request) {
        Pageable pageable = ReactiveUtils.extractPageable(request);
        return networkService.getMyNetworks_bna(pageable)
                        .flatMap(networkListRows -> ServerResponse.ok().bodyValue(networkListRows))
                        .doOnError(error -> log.error("Error retrieving networks for company", error));
    }

    public Mono<ServerResponse> getMyNetworks_bu(ServerRequest request) {
        Pageable pageable = ReactiveUtils.extractPageable(request);
        return networkService.getMyNetworks_bu(pageable)
                .flatMap(networkListRows -> ServerResponse.ok().bodyValue(networkListRows))
                .doOnError(error -> log.error("Error retrieving networks for company", error));

    }

    public Mono<ServerResponse> getMyNetworks_tpa(ServerRequest request) {
        Pageable pageable = ReactiveUtils.extractPageable(request);
        return networkService.getMyNetworks_tpa(pageable)
                .flatMap(networkListRows -> ServerResponse.ok().bodyValue(networkListRows))
                .doOnError(error -> log.error("Error retrieving networks for company", error));
    }

    public Mono<ServerResponse> getMyInvitations(ServerRequest serverRequest) {
        Pageable pageable = ReactiveUtils.extractPageable(serverRequest);
        return networkService.getMyInvitations(pageable)
                .flatMap(invitations -> ServerResponse.ok().bodyValue(invitations))
                .doOnError(error -> log.error("Error retrieving invitations", error));
    }

    public Mono<ServerResponse> getClients(ServerRequest serverRequest) {
        Pageable pageable = ReactiveUtils.extractPageable(serverRequest);
        return networkService.getClients(serverRequest.pathVariable("networkId"), pageable)
                .flatMap(clients -> ServerResponse.ok().bodyValue(clients))
                .doOnError(error -> log.error("Error retrieving clients", error));
    }

    public Mono<ServerResponse> getSuppliers(ServerRequest serverRequest) {
        Pageable pageable = ReactiveUtils.extractPageable(serverRequest);
        return networkService.getSuppliers(serverRequest.pathVariable("networkId"), pageable)
                .flatMap(suppliers -> ServerResponse.ok().bodyValue(suppliers))
                .doOnError(error -> log.error("Error retrieving suppliers", error));
    }

    public Mono<ServerResponse> getOtherMembers_bna(ServerRequest serverRequest) {
        Pageable pageable = ReactiveUtils.extractPageable(serverRequest);
        return networkService.getOtherMembers_bna(serverRequest.pathVariable("networkId"), pageable)
                .flatMap(suppliers -> ServerResponse.ok().bodyValue(suppliers))
                .doOnError(error -> log.error("Error retrieving other members", error));
    }

    public Mono<ServerResponse> getRefused(ServerRequest serverRequest) {
        Pageable pageable = ReactiveUtils.extractPageable(serverRequest);
        String networkId = serverRequest.pathVariable("networkId");

        return networkService.getRefused(networkId, pageable)
                .flatMap(page -> ServerResponse.ok().bodyValue(page))
                .doOnError(error -> log.error("Error retrieving refused nodes", error));
    }

    public Mono<ServerResponse> requestVerification(ServerRequest serverRequest) {
        String networkId = serverRequest.pathVariable("networkId");

        return serverRequest.bodyToMono(RequestVerificationDto.class)
                .flatMap(body -> networkService.requestVerification(networkId, body.auditorId()))
                .then(ServerResponse.noContent().build())
                .doOnError(err -> log.error("Error requesting verification", err));
    }


    public Mono<ServerResponse> create(ServerRequest request) {
        return getBodyFromRequest(request, CreateNetworkDto.class)
                .flatMap(createNetworkDto -> networkService.create(createNetworkDto)
                        .flatMap(network -> ServerResponse.status(201).bodyValue(network))
                        .doOnError(error -> log.error("Error creating network", error)));
    }

    public Mono<ServerResponse> getNetworkDetails(ServerRequest serverRequest) {
        return networkService.getNetworkDetails(serverRequest.pathVariable("networkId"))
                .flatMap(networkDetails -> ServerResponse.ok().bodyValue(networkDetails))
                .doOnError(error -> log.error("Error retrieving network details", error));
    }

    public Mono<ServerResponse> getNetworkDetails_tpa(ServerRequest serverRequest) {
        return networkService.getNetworkDetails_tpa(serverRequest.pathVariable("networkId"))
                .flatMap(networkDetails -> ServerResponse.ok().bodyValue(networkDetails))
                .doOnError(error -> log.error("Error retrieving network details", error));
    }

    public Mono<ServerResponse> acceptInvitation(ServerRequest serverRequest) {
        return networkService.acceptInvitation(serverRequest.pathVariable("networkId"))
                .flatMap(acceptedNetwork -> ServerResponse.ok().build())
                .doOnError(error -> log.error("Error accepting invitation", error));
    }

    public Mono<ServerResponse> refuseInvitation(ServerRequest serverRequest) {
        return networkService.refuseInvitation(serverRequest.pathVariable("networkId"))
                .flatMap(acceptedNetwork -> ServerResponse.ok().build())
                .doOnError(error -> log.error("Error accepting invitation", error));
    }

    public Mono<ServerResponse> getAuditors(ServerRequest serverRequest) {
        return networkService.getAuditors(serverRequest.pathVariable("networkId"))
                .collectList()
                .flatMap(auditors -> ServerResponse.ok().bodyValue(auditors))
                .doOnError(error -> log.error("Error retrieving auditors", error));
    }

    public Mono<ServerResponse> requestAudit(ServerRequest serverRequest) {
        return getBodyFromRequest(serverRequest, RequestAuditDto.class)
                .flatMap(requestAuditDto -> networkService.requestAudit(serverRequest.pathVariable("networkId"), requestAuditDto))
                .flatMap(auditRequest -> ServerResponse.ok().bodyValue(auditRequest))
                .doOnError(error -> log.error("Error requesting audit", error));
    }

    public Mono<ServerResponse> addAuditors(ServerRequest serverRequest) {
        return getBodyFromRequest(serverRequest, CreateNetworkDto.class)
                .flatMap(createNetworkDto -> networkService.addAuditors(serverRequest.pathVariable("networkId"), createNetworkDto)
                        .flatMap(response -> ServerResponse.ok().bodyValue(response))
                        .doOnError(error -> log.error("Error creating network", error)));
    }

    public Mono<ServerResponse> computeEResult(ServerRequest serverRequest) {
        return  networkService.computeEResult(serverRequest.pathVariable("networkId"))
                        .flatMap(response -> ServerResponse.ok().bodyValue(response))
                        .doOnError(error -> log.error("Error creating network", error));
    }

    public Mono<ServerResponse> computeScope3(ServerRequest serverRequest) {
        return getBodyFromRequest(serverRequest, ComputeValue.class)
                .flatMap(computeScope3dto ->  networkService.computeScope3(serverRequest.pathVariable("networkId"), computeScope3dto))
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnError(error -> log.error("Error creating network", error));
    }

    public Mono<ServerResponse> updateNode(ServerRequest serverRequest) {
        return getBodyFromRequest(serverRequest, UpdateNodeValuesDto.class)
                .flatMap(updateNodeValuesDto -> networkService.updateNode(serverRequest.pathVariable("nodeId"), updateNodeValuesDto)
                        .flatMap(response -> ServerResponse.ok().bodyValue(response))
                        .doOnError(error -> log.error("Error creating network", error)));
    }

    public Mono<ServerResponse> getNetworkTree_bna(ServerRequest serverRequest) {
        return networkService.getNetworkTree_bna(serverRequest.pathVariable("networkId"))
                .flatMap(networkTree -> ServerResponse.ok().bodyValue(networkTree))
                .doOnError(error -> log.error("Error retrieving network tree", error));
    }

    public Mono<ServerResponse> getNetworkTree_bu(ServerRequest serverRequest) {
        return networkService.getNetworkTree_bu(serverRequest.pathVariable("networkId"))
                .flatMap(networkTree -> ServerResponse.ok().bodyValue(networkTree))
                .doOnError(error -> log.error("Error retrieving network tree", error));
    }


    public Mono<ServerResponse> updateNetworkTree(ServerRequest req) {
        String networkId = req.pathVariable("networkId");
        return getBodyFromRequest(req, UpdateNetworkTreeDto.class)
                .flatMap(dto -> networkService.updateNetworkTree(networkId, dto))
                .flatMap(tree -> ServerResponse.ok()
                        .bodyValue(tree))
                .doOnError(error -> log.error("Error updating network tree", error));
    }

    public Mono<ServerResponse> getMyRequests(ServerRequest request) {
        String networkId = request.pathVariable("networkId");
        Pageable pageable = ReactiveUtils.extractPageable(request);

        return networkService.getMyRequests(networkId, pageable)
                .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    public Mono<ServerResponse> getOtherMembers_tpa(ServerRequest request) {
        String networkId = request.pathVariable("networkId");
        Pageable pageable = ReactiveUtils.extractPageable(request);
        return networkService.getOtherMembers_tpa(networkId, pageable)
                .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    public Mono<ServerResponse> getNodeDetails(ServerRequest request) {
        String nodeId = request.pathVariable("nodeId");
        return networkService.getNodeDetails(nodeId)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> verifyData_tpa(ServerRequest request) {
        String nodeId = request.pathVariable("nodeId");
        return networkService.verifyData_tpa(nodeId)
                .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }


    public Mono<ServerResponse> getMetadata(ServerRequest serverRequest) {
        String metadataHash = serverRequest.pathVariable("metadataHash");
        return networkService.getMetadata(metadataHash)
                .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    public Mono<ServerResponse> computeMerkleHash(ServerRequest request) {
        return getBodyFromRequest(request, NetworkTreeDto.class)
                .map(TreeHasher::hashNodeUnordered)
                .flatMap(hash -> ServerResponse.ok().bodyValue(Map.of("hash", hash)))
                .doOnError(error -> log.error("Error computing Merkle hash", error));
    }

    public Mono<ServerResponse> checkNameExistence(ServerRequest request) {
        return networkService.checkNameExistence(request.pathVariable("name"))
                .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }


}
