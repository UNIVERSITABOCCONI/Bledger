package it.bocconi.bledger.feature.network.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.exception.BadRequestException;
import it.bocconi.bledger.exception.ForbiddenException;
import it.bocconi.bledger.exception.NotFoundException;
import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.company.router.dto.CompanyLightDto;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.service.FileService;
import it.bocconi.bledger.feature.network.dao.repository.row.*;
import it.bocconi.bledger.feature.network.dao.service.*;
import it.bocconi.bledger.feature.network.entity.*;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import it.bocconi.bledger.feature.network.mapper.NetworkMapper;
import it.bocconi.bledger.feature.network.router.dto.*;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcBLedgerNetworkDaoService;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcBLedgerNetworkMetadata;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.smartcontract.service.BLedgerNetworkService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.router.dto.TransactionOperationWrapper;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;

import javax.xml.bind.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NetworkService {

    private final BcNetworkDaoService bcNetworkDaoService;
    private final BcNodeDaoService bcNodeDaoService;
    private final BcNetworkAuditorDaoService bcNetworkAuditorDaoService;
    private final ObjectMapper objectMapper;
    private final BcNetworkTreeCacheDaoService bcNetworkTreeCacheDaoService;
    private final NetworkMapper networkMapper;
    private final BcNodeMetadataDaoService bcNodeMetadataDaoService;
    private final BcFileDaoService bcFileDaoService;
    private final long expirationDays;
    private final BLedgerNetworkService bLedgerNetworkService;
    private final BcBLedgerNetworkDaoService bcBLedgerNetworkDaoService;
    private final TransactionService transactionService;
    private final BcSmartContractDaoService bcSmartContractDaoService;
    private final FileService fileService;


    public NetworkService(BcNetworkDaoService bcNetworkDaoService,
                          BcNodeDaoService bcNodeDaoService,
                          BcNetworkAuditorDaoService bcNetworkAuditorDaoService,
                          ObjectMapper objectMapper,
                          BcNetworkTreeCacheDaoService bcNetworkTreeCacheDaoService,
                          NetworkMapper networkMapper,
                          BcNodeMetadataDaoService bcNodeMetadataDaoService,
                          BcFileDaoService bcFileDaoService,
                          @Value("${it.bocconi.bledger.job.approval-expiration.approval-expiration-days:3}") long expirationDays,
                          BLedgerNetworkService bLedgerNetworkService,
                          BcBLedgerNetworkDaoService bcBLedgerNetworkDaoService,
                          TransactionService transactionService,
                          BcSmartContractDaoService bcSmartContractDaoService,
                          FileService fileService) {
        this.bcNetworkDaoService = bcNetworkDaoService;
        this.bcNodeDaoService = bcNodeDaoService;
        this.bcNetworkAuditorDaoService = bcNetworkAuditorDaoService;
        this.objectMapper = objectMapper;
        this.bcNetworkTreeCacheDaoService = bcNetworkTreeCacheDaoService;
        this.bcNodeMetadataDaoService = bcNodeMetadataDaoService;
        this.bcFileDaoService = bcFileDaoService;
        this.expirationDays = expirationDays;
        this.networkMapper = networkMapper;
        this.bLedgerNetworkService = bLedgerNetworkService;
        this.bcBLedgerNetworkDaoService = bcBLedgerNetworkDaoService;
        this.transactionService = transactionService;
        this.bcSmartContractDaoService = bcSmartContractDaoService;
        this.fileService = fileService;
    }

    @Transactional(readOnly = true)
    public Mono<Page<NetworkTableElementRow>> getMyNetworks_bna(Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap((companyId) ->
                        bcNetworkDaoService.getMyNetworks_bna(pageable, companyId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NetworkTableElementRow>> getMyNetworks_bu(Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap((companyId) ->
                        bcNetworkDaoService.getMyNetworks_bu(pageable, companyId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NetworkTableElementRowTpa>> getMyNetworks_tpa(Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap((companyId) ->
                        bcNetworkDaoService.getMyNetworks_tpa(pageable, companyId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NetworkTableElementRow>> getMyInvitations(Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap((companyId) ->
                        bcNetworkDaoService.getInvitations(pageable, companyId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NodeLightDto>> getClients(String networkId, Pageable pageable) {
        return getNodeByNetworkId(networkId)
                .flatMap(node -> bcNodeDaoService.getClients(pageable, node.getParentId()))
                .map(clients -> clients.map(networkMapper::toNodeLightDto));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NodeLightDto>> getOtherMembers_bna(String networkId, Pageable pageable){
        return getNodeByNetworkId(networkId)
                .flatMap(node -> bcNodeDaoService.getOtherMembers_bna(pageable, networkId, node.getId(), node.getParentId()))
                .map(members -> members.map(networkMapper::toNodeLightDto));

    }

    @Transactional(readOnly = true)
    public Mono<Page<NodeLightDto>> getRefused(String networkId, Pageable pageable) {
        return bcNodeDaoService.getRefused(pageable, networkId)
                .map(page -> page.map(networkMapper::toNodeLightDto));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NodeLightDto>> getSuppliers(String networkId, Pageable pageable) {
        return getNodeByNetworkId(networkId)
                .flatMap(node -> bcNodeDaoService.getSuppliers(pageable, node.getId()))
                .map(clients -> clients.map(networkMapper::toNodeLightDto));
    }

    @Transactional(readOnly = true)
    public Mono<String> getMetadata(String hash) {
        return bcBLedgerNetworkDaoService.findByContentHash(hash)
                .map(BcBLedgerNetworkMetadata::getContent);
    }

    @Transactional(readOnly = true)
    public Mono<NetworkDetailsDto> getNetworkDetails(String networkId) {

        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNodeDaoService.findDetailsByNetworkIdAndCompanyName(networkId, companyId)
                )
                .flatMap(nodeDetailsRow -> Mono.zip(
                                bcNodeDaoService.findChildrenCompletionPercentage(nodeDetailsRow.getId()),
                                bcNodeDaoService.findChildrenEComputationPercentage(nodeDetailsRow.getId()),
                                bcSmartContractDaoService.findByType(SmartContractType.ERC721),
                                bcNodeDaoService.getChildrenLastComputeChange(nodeDetailsRow.getId())
                                        .map(Optional::of)
                                        .switchIfEmpty(Mono.just(Optional.empty()))
                        )
                        .map(tuple -> mapNetworkDetailsDto(nodeDetailsRow, tuple)));

    }

    @Transactional(readOnly = true)
    public Mono<NetworkDetailsDto> getNetworkDetails_tpa(String networkId) {
        return RequestContext.getCompanyId()
                .flatMap(companyId -> Mono.zip(
                        bcNetworkDaoService.findById(networkId),
                        bcSmartContractDaoService.findByType(SmartContractType.ERC721)
                        )
                )
                .map(tuple -> new NetworkDetailsDto(
                        networkId,
                        tuple.getT1().getNetworkName(),
                        false,
                        null,
                        null,
                        tuple.getT1().getTokenId(),
                        tuple.getT2().getContractAddress(),
                        null,
                        null,
                        null,
                        null,
                        null
                ));
    }

    @Transactional(readOnly = true)
    public Mono<NetworkTreeDto> getNetworkTree_bna(String networkId) {
        return bcNetworkTreeCacheDaoService.findByNetworkId(networkId)
                .map(bcNetworkTreeCache -> {
                            try {
                                return objectMapper.readValue(bcNetworkTreeCache.getTreeJson(), NetworkTreeDto.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Error deserializing network tree from JSON", e);
                            }
                        }
                );
    }

    @Transactional(readOnly = true)
    public Mono<NetworkTreeDto> getNetworkTree_bu(String networkId) {
        return getNodeWithCompanyNameByNetworkId(networkId)
                .flatMap(node -> bcNodeDaoService.findParentAndChildren(node.getId(), node.getParentId())
                        .collectList()
                        .map(nodeList -> {
                            List<NodeWithCompanyNameRow> children;
                            NetworkTreeDto root;
                            NetworkTreeDto currentNode = networkMapper.toNetworkElementDto(node);
                            if(nodeList.isEmpty()){
                                return currentNode;
                            } else {
                                if(nodeList.getFirst().getId().equals(node.getParentId())){
                                    root = networkMapper.toNetworkElementDto(nodeList.getFirst());
                                    root.setChildren(List.of(currentNode));
                                    children = nodeList.subList(1, nodeList.size());
                                } else {
                                    root = currentNode;
                                    children = nodeList;
                                }
                                currentNode.setChildren(children.stream().map(networkMapper::toNetworkElementDto).collect(Collectors.toList()));
                                return root;
                            }
                        })
                );
    }

    @Transactional(readOnly = true)
    public Mono<Page<GetAuditorRequestsDto>> getMyRequests(String networkId, Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap(auditorCompanyId ->
                        bcNodeDaoService.getMyRequests(pageable, networkId, auditorCompanyId)
                                .map(p -> p.map(networkMapper::toGetAuditorRequestsDto))
                );
    }

    @Transactional(readOnly = true)
    public Mono<Page<NodeLightDto>> getOtherMembers_tpa(String networkId, Pageable pageable) {
        return RequestContext.getCompanyId()
                .flatMap(auditorCompanyId ->
                        bcNodeDaoService.getOtherMembers_tpa(pageable, networkId, auditorCompanyId)
                                .map(p -> p.map(networkMapper::toNodeLightDto))
                );
    }

    @Transactional(readOnly = true)
    public Mono<TpaNodeDetailsDto> getNodeDetails(String nodeId) {
        return RequestContext.getCompanyId()
                .flatMap(
                        companyId -> bcNodeDaoService.findById(nodeId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Node not found: " + nodeId)))
                                .flatMap(node -> {
                                    if (!(companyId.equals(node.getCompanyId()) ||
                                            companyId.equals(node.getAuditorId()))) {
                                        return Mono.error(new BadRequestException("You must be owner or auditor to access node details"));
                                    }
                                    Mono<BcFile> scopeM = safeFindFile(node.getScopeFileId());
                                    Mono<BcFile> g1M    = safeFindFile(node.getGranularScope1FileId());
                                    Mono<BcFile> g2M    = safeFindFile(node.getGranularScope2FileId());
                                    Mono<List<BcNodeMetadata>> metaM =
                                            bcNodeMetadataDaoService.findByNodeIdAndType(node.getId(), NodeMetadataType.GLOBAL_SCOPE)
                                                    .collectList();

                                    return Mono.zip(scopeM, g1M, g2M, metaM)
                                            .map(t -> {
                                                BcFile scope = t.getT1();
                                                BcFile g1    = t.getT2();
                                                BcFile g2    = t.getT3();
                                                List<BcNodeMetadata> metas = t.getT4();
                                                List<NodeMetadataDto> result;
                                                if(metas.isEmpty() && node.getNodeStatus() == NodeStatus.ACCEPTED){
                                                    result = fileService.getMetadataDtoFromFile(scope);
                                                } else {
                                                    result =                                      metas.stream()
                                                            .map(m -> new NodeMetadataDto(
                                                                    m.getMetadataKey(),
                                                                    m.getMetadataValue(),
                                                                    m.getUnitOfMeasure()
                                                            ))
                                                            .toList();
                                                }

                                                return new TpaNodeDetailsDto(
                                                        fileLight(scope, node.getUploadFileTransactionId()),
                                                        fileLight(g1, node.getUploadFileTransactionId()),
                                                        fileLight(g2, node.getUploadFileTransactionId()),
                                                        result
                                                );
                                            });
                                })
                );

    }

    @Transactional
    public Mono<Void> requestVerification(String networkId, String auditorId) {
        return bcNetworkAuditorDaoService.findByNetworkIdAndCompanyId(networkId, auditorId)
                .switchIfEmpty(Mono.error(new BadRequestException("Auditor not part of this network")))
                .flatMap(networkAuditor ->
                        // increment requests_count
                        bcNetworkAuditorDaoService.incrementRequestsCount(networkAuditor.getId())
                                .then(getNodeByNetworkId(networkId)
                                        .flatMap(node -> {
                                            node.setAuditorId(auditorId);
                                            return bcNodeDaoService.save(node);
                                        })
                                ).then()
                );
    }

    @Transactional(readOnly = true)
    public Mono<AlreadyExistsDto> checkNameExistence(String name){
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNetworkDaoService.existsByNameAndNetworkAdminId(name, companyId)).map(AlreadyExistsDto::new);
    }


    @Transactional
    public Mono<TransactionOperationWrapper<NetworkDto>> create(CreateNetworkDto dto) {
        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNetworkDaoService.existsByNameAndNetworkAdminId(dto.name(), companyId)
                                .flatMap(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        return Mono.error(new ValidationException("Network name already exists"));
                                    }
                                    return bcNetworkDaoService.save(buildNetworkEntity(dto, companyId))
                                            .flatMap(network ->
                                                    Mono.zip(
                                                            bcNetworkAuditorDaoService
                                                                    .saveAll(buildNetworkAuditors(dto.auditorIds(), network.getId()))
                                                                    .collectList(),
                                                            createNetworkTree(network.getId(), dto.tree())
                                                                    .flatMap(tree ->
                                                                            sanityCheckTree(network.getId(), network.getNetworkAdminId())
                                                                                    .thenReturn(tree)
                                                                    )
                                                    ).map(tuple -> {
                                                        var treeResult = tuple.getT2();
                                                        return new TransactionOperationWrapper<>(
                                                                treeResult.getTransactionId(),
                                                                networkMapper.toNetworkDto(network, treeResult.getContent())
                                                        );
                                                    })
                                            );
                                })
                );
    }


    @Transactional
    public Mono<Void> acceptInvitation(String networkId){
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Node not found for network " + networkId + " and company " + companyId)))
                        .flatMap(node -> {
                            node.setNodeStatus(NodeStatus.ACCEPTED);
                            return bcNodeDaoService.save(node)
                                    .then(bcNodeDaoService.setApprovalExpirationDateToChildrenById(node.getId(), LocalDate.now().plusDays(expirationDays)))
                                    .then();
                        })
                );
    }

    @Transactional
    public Mono<Void> refuseInvitation(String networkId){
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Node not found for network " + networkId + " and company " + companyId)))
                        .flatMap(this::refuseNode)
                .then(bcNetworkTreeCacheDaoService.deleteByNetworkId(networkId))
                .then(Mono.zip(rebuildNetworkTreeCacheAndReturnTree(networkId), bcNetworkDaoService.reloadAllCounts(networkId))
                        .map(Tuple2::getT1)
                )
                .flatMap(tree -> bLedgerNetworkService.createBcBLedgerMetadata(networkId)
                .flatMap(metadata -> transactionService.createTransaction(TransactionType.REFUSED_INVITATION, companyId,null, metadata.getId(), networkId))
                .map(tx -> new TransactionOperationWrapper<>(tx.getId(), tree)))).then();
    }

    @Transactional
    public Flux<CompanyLightDto> getAuditors(String networkId) {
        return bcNetworkAuditorDaoService.findByCompanyRowLightByNetworkId(networkId)
                .map(auditor -> new CompanyLightDto(
                        auditor.getId(),
                        auditor.getCompanyName()));
    }

    @Transactional
    public Mono<RequestAuditDto> requestAudit(String networkId, RequestAuditDto requestAuditDto) {
        return getNodeByNetworkId(networkId)
                .flatMap(node -> {
                    if (node.getNodeStatus() != NodeStatus.FILE_UPLOADED) {
                        return Mono.error(new BadRequestException("You must accept the invitation and upload files to request an audit"));
                    }
                    return bcNetworkAuditorDaoService.findByNetworkIdAndCompanyId(networkId, requestAuditDto.auditorId())
                            .switchIfEmpty(Mono.error(new BadRequestException("The auditor you are trying to request an audit from is not part of the network " + networkId)))
                            .flatMap(networkAuditor -> {

                                node.setAuditorId(requestAuditDto.auditorId());
                                return Mono.when(bcNodeDaoService.save(node), bcNetworkAuditorDaoService.incrementRequestsCount(networkAuditor.getId())).thenReturn(requestAuditDto);
                            });
                });
    }

    @Transactional
    public Mono<TransactionOperationWrapper<?>> addAuditors(String networkId, CreateNetworkDto createNetworkDto) {
        List<BcNetworkAuditor> auditors =
                buildNetworkAuditors(createNetworkDto.auditorIds(), networkId);

        return bcNetworkAuditorDaoService.saveAll(auditors)
                .collectList()
                .flatMap(savedAuditors ->
                        RequestContext.getCompanyId()
                                .flatMap(companyId ->
                                        getBcTransactionMono(networkId, companyId, savedAuditors)
                                                .map(tx -> new TransactionOperationWrapper<>(tx.getId(), null))
                                )
                );
    }

    @Transactional
    public Mono<ComputeValue> computeEResult(String networkId) {
        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                                .switchIfEmpty(Mono.error(new BadRequestException("you are not part of this network")))
                                .flatMap(node -> {

                                    // parse scope1 + scope2 -> base
                                    Mono<BigDecimal> base = bigDecimalFromScope(node.getScope1())
                                            .zipWith(
                                                    bigDecimalFromScope(node.getScope2()),
                                                    BigDecimal::add
                                            )
                                            .onErrorMap(NumberFormatException.class,
                                                    e -> new BadRequestException("your scope1 and/or scope2 values are not numbers"));

                                    // sum children parts
                                    Mono<BigDecimal> partsSum = bcNodeDaoService.findByParentId(node.getId()) // Flux<BcNode>
                                            .flatMap(child -> {
                                                if (child.getQuantity() == null
                                                        || child.getTransportationEmission() == null
                                                        || child.getEValue() == null) {
                                                    return Mono.error(new BadRequestException("Missing data"));
                                                }
                                                BigDecimal internal = child.getEValue().add(child.getTransportationEmission());
                                                return Mono.just(internal.multiply(child.getQuantity()));
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add); // empty -> ZERO

                                    // combine and wrap into record
                                    return Mono.zip(base, partsSum, BigDecimal::add)
                                            .flatMap(eVaule -> {
                                                node.setEValue(eVaule);
                                                node.setLastCompute(LocalDateTime.now());
                                                node.setLastComputeChange(LocalDateTime.now());
                                                return bcNodeDaoService.save(node)
                                                        .thenReturn(new ComputeValue(eVaule));
                                            });
                                })
                );
    }

    @Transactional
    public Mono<ComputeValue> computeScope3(String networkId, ComputeValue computeValue) {
        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                                .switchIfEmpty(Mono.error(new BadRequestException("you are not part of this network")))
                                .flatMap(node -> {
                                    BigDecimal eValue = node.getEValue();
                                    if (eValue == null){
                                        return Mono.error(new BadRequestException("Missing e value"));
                                    }

                                    if(computeValue.value() == null){
                                        return Mono.error(new BadRequestException("Missing production volume"));
                                    }
                                    BigDecimal result = eValue.multiply(computeValue.value());

                                    node.setProductionVolume(computeValue.value());
                                    node.setScope3(result);

                                    return bcNodeDaoService.save(node).thenReturn(new ComputeValue(result));
                                })
                );
    }

    @Transactional
    public Mono<Void> updateNode(String nodeId, UpdateNodeValuesDto nodeDto) {
        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNodeDaoService.findById(nodeId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Node not found: " + nodeId)))
                                .flatMap(node ->
                                        bcNodeDaoService.findById(node.getParentId())
                                                .switchIfEmpty(Mono.error(
                                                        new ForbiddenException("You can change quantity and transport emissions only for your suppliers")))
                                                .flatMap(parent -> {
                                                    if (!parent.getCompanyId().equals(companyId)) {
                                                        return Mono.error(new ForbiddenException(
                                                                "You can change quantity and transport emissions only for your suppliers"));
                                                    }
                                                    node.setTransportationEmission(nodeDto.transportEmissions());
                                                    node.setQuantity(nodeDto.quantity());
                                                    node.setLastComputeChange(LocalDateTime.now());
                                                    return bcNodeDaoService.save(node).then();
                                                })
                                )
                );
    }

    @Transactional
    public Mono<TransactionOperationWrapper<NetworkTreeDto>> updateNetworkTree(String networkId, UpdateNetworkTreeDto dto) {
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNetworkDaoService.findById(networkId)
                    .switchIfEmpty(Mono.error(new NotFoundException("Network not found: " + networkId)))
                    .flatMap(net -> updateTree(networkId, dto.updatedTree()).then(deleteNodes(dto.removed())).then(sanityCheckTree(networkId, companyId)))
                    // authoritative rebuild from DB → NetworkTreeDto
                    .then(Mono.zip(
                            bcNetworkDaoService.reloadAllCounts(networkId),
                            bcNetworkTreeCacheDaoService.deleteByNetworkId(networkId),
                            bcNetworkAuditorDaoService.reloadAllCounts(networkId)
                            )
                            .then(rebuildNetworkTreeCacheAndReturnTree(networkId))
                            .flatMap(tree -> bLedgerNetworkService.createBcBLedgerMetadata(networkId)
                                    .flatMap(metadata -> transactionService.createTransaction(TransactionType.UPDATE_NETWORK, companyId,null, metadata.getId(), networkId))
                                    .map(tx -> new TransactionOperationWrapper<>(tx.getId(), tree))))
                );
    }

    @Transactional
    public Mono<TransactionOperationWrapper<NodeDto>> verifyData_tpa(String nodeId) {
        return RequestContext.getCompanyId()
                .flatMap(currentAuditorId ->
                        bcNodeDaoService.findById(nodeId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Node not found: " + nodeId)))
                                .flatMap(node -> {
                                    // Authorization: only the assigned auditor can verify
                                    if (node.getAuditorId() == null || !node.getAuditorId().equals(currentAuditorId)) {
                                        return Mono.error(new ForbiddenException("You are not the assigned auditor for this node"));
                                    }

                                    // Update node -> AUDITED
                                    node.setNodeStatus(NodeStatus.AUDITED);
                                    node.setAudited(true);
                                    node.setAuditedAt(java.time.LocalDateTime.now());

                                    Mono<BcNode> saveNode = bcNodeDaoService.save(node);

                                    Mono<BcTransaction> txMono =
                                            bcNetworkAuditorDaoService.findByNetworkIdAndCompanyId(node.getNetworkId(), currentAuditorId)
                                                    .flatMap(networkAuditor ->
                                                            Mono.zip(
                                                                    bcNetworkAuditorDaoService.incrementAuditedCount(networkAuditor.getId()),
                                                                            bcNetworkDaoService.incrementAuditedCount(node.getNetworkId())
                                                                    )
                                                                    .flatMap(count -> bLedgerNetworkService.createBcBLedgerMetadata(node.getNetworkId())
                                                                            .flatMap(metadata ->
                                                                                    transactionService.createTransaction(TransactionType.AUDIT, networkAuditor.getAuditorId(), null, metadata.getId(), node.getNetworkId(), node.getId()))
                                                                            .flatMap(tx -> {
                                                                                node.setAuditFileTransactionId(tx.getId());
                                                                                return bcNodeDaoService.save(node).thenReturn(tx);
                                                                            })
                                                                    )
                                                    );

                                    return Mono.zip(saveNode, txMono).
                                            map(tuple -> {
                                                BcNode nodeSaved = tuple.getT1();
                                                BcTransaction tx = tuple.getT2();
                                                return new TransactionOperationWrapper<>(tx.getId(), networkMapper.toNodeDto(nodeSaved));
                                            });
                                })
                );
    }





    private FileLightDto fileLight(BcFile file, String uploadTxId) {
        return file == null ? null : new FileLightDto(file.getId(), file.getFileName(), uploadTxId, file.getSize());
    }

    private Mono<BcFile> safeFindFile(String fileId) {
        return fileId == null ? Mono.empty() : bcFileDaoService.findById(fileId);
    }

    private BcNetwork buildNetworkEntity(CreateNetworkDto createNetworkDto, String companyId) {
        return BcNetwork.builder()
                .networkAdminId(companyId)
                .networkName(createNetworkDto.name())
                .networkAdminId(companyId)
                .build();
    }



    private List<BcNetworkAuditor> buildNetworkAuditors(List<String> auditorIds, String networkId) {
        return auditorIds.stream()
                .map(auditorId -> BcNetworkAuditor.builder()
                        .auditorId(auditorId)
                        .networkId(networkId)
                        .build())
                .collect(Collectors.toList());
    }

    private BcNetworkTreeCache buildNetworkTreeCache(String networkId, NetworkTreeDto treeNode) {

        String treeJson;
        try {
            treeJson = objectMapper.writeValueAsString(treeNode);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing network tree to JSON", e);
        }
        return BcNetworkTreeCache.builder()
                .networkId(networkId)
                .treeJson(treeJson)
                .build();
    }

    private Mono<BcTransaction> getBcTransactionMono(String networkId, String companyId, List<BcNetworkAuditor> auditors) {
        try {
            return transactionService.createTransaction(TransactionType.ADD_AUDITORS, companyId, null, objectMapper.writeValueAsString(auditors.stream().map(BcNetworkAuditor::getAuditorId).collect(Collectors.toList())), networkId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private Mono<BcNode> getNodeByNetworkId(String networkId) {
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                        .switchIfEmpty(Mono.error(new BadRequestException("You are not a member of the network " + networkId))));
    }

    private Mono<NodeWithCompanyNameRow> getNodeWithCompanyNameByNetworkId(String networkId) {
        return RequestContext.getCompanyId()
                .flatMap(companyId -> bcNodeDaoService.findByNetworkIdAndCompanyIdWithCompanyName(networkId, companyId)
                        .switchIfEmpty(Mono.error(new BadRequestException("You are not a member of the network " + networkId))));
    }

    private NetworkTreeDto toTree(NodeWithCompanyNameRow r, Map<String, List<NodeWithCompanyNameRow>> byParent) {
        NetworkTreeDto dto = NetworkTreeDto.builder()
                .id(r.getId())
                .parentId(r.getParentId())
                .companyId(r.getCompanyId())
                .companyName(r.getCompanyName())
                .nodeDepth(r.getNodeDepth())
                .children(new ArrayList<>())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();

        for (var child : byParent.getOrDefault(r.getId(), List.of())) {
            dto.getChildren().add(toTree(child, byParent));
        }
        return dto;
    }

    private Mono<Void> refuseChildren(BcNode node) {
        node.setNodeStatus(NodeStatus.REFUSED_BY_PARENT);
        node.setDeleted(true);
        node.setDeletedAt(LocalDateTime.now());
        return bcNodeDaoService.save(node)
                .then(bcNodeDaoService.findByParentId(node.getId())
                        .flatMap(child -> bcNodeDaoService.findById(child.getId())
                                .flatMap(this::refuseChildren)
                        ).collectList().then());
    }

    private Flux<BcNode> traverseAndCollect(NetworkTreeDto treeNode,
                                            String parentId,
                                            int depth,
                                            String networkId,
                                            String currentCompanyId,
                                            LocalDateTime now
    ) {

        //if is the node is admin, should be already accepted
        NodeStatus status = treeNode.getCompanyId().equals(currentCompanyId) ? NodeStatus.ACCEPTED : NodeStatus.INVITED;

        BcNode node = BcNode.builder()
                .networkId(networkId)
                .parentId(parentId)
                .companyId(treeNode.getCompanyId())
                .nodeStatus(status)
                .audited(false)
                .nodeDepth(depth)
                .build();

        if(depth == 1){
            node.setApprovalExpirationDate(LocalDate.now().plusDays(expirationDays));
        }

        return bcNodeDaoService.save(node)
                .doOnNext(savedNode -> {
                    treeNode.setId(savedNode.getId());
                    treeNode.setNodeDepth(depth);
                    treeNode.setParentId(parentId);
                    treeNode.setNetworkId(networkId);
                    treeNode.setCreatedAt(now);
                    treeNode.setUpdatedAt(now);
                })
                .flatMapMany(savedNode ->
                        Flux.concat(
                                Flux.just(savedNode),
                                Flux.fromIterable(treeNode.getChildren())
                                        .flatMap(child -> traverseAndCollect(child, savedNode.getId(), depth + 1, networkId, currentCompanyId, now))
                        )
                );
    }

    private static NetworkDetailsDto mapNetworkDetailsDto(
            NodeDetailsRow nodeDetailsRow,
            Tuple4<Double, ComputeEPercentageRow, BcSmartContract, Optional<LocalDateTime>> tuple
    ) {
        Double completion = tuple.getT1();

        long suppliersCount = tuple.getT2().getCount();

        double ratio = tuple.getT2().getRatio();
        double supplierFactor = (suppliersCount >= 0) ? (suppliersCount / (double) (suppliersCount + 1)) : 0.0d;

        double computeEcompletion = ratio * supplierFactor * 100.0d;

        if (nodeDetailsRow.getScopeFileId() != null) {
            computeEcompletion += (100.0d / (suppliersCount + 1));
        }

        computeEcompletion = Math.round(computeEcompletion * 100.0) / 100.0;

        BcSmartContract erc721 = tuple.getT3();

        Optional<LocalDateTime> lastComputeVersionOpt = tuple.getT4();

        LocalDateTime lastComputeVersion = lastComputeVersionOpt.orElse(null);

        return new NetworkDetailsDto(
                nodeDetailsRow.getNetworkId(),
                nodeDetailsRow.getNetworkName(),
                nodeDetailsRow.getNetworkAdminId().equals(nodeDetailsRow.getCompanyId()),
                completion,
                computeEcompletion,
                nodeDetailsRow.getTokenId(),
                erc721.getContractAddress(),
                nodeDetailsRow.getLastExport(),
                nodeDetailsRow.getLastCompute(),
                lastComputeVersion, //FIXME: should change with BM-47
                lastComputeVersion,
                new NodeDto(
                        nodeDetailsRow.getId(),
                        nodeDetailsRow.getParentId(),
                        null,
                        nodeDetailsRow.getScopeFileId(),
                        nodeDetailsRow.getScopeFileName(),
                        nodeDetailsRow.getGranularScope1FileId(),
                        nodeDetailsRow.getGranularScope1FileName(),
                        nodeDetailsRow.getGranularScope2FileId(),
                        nodeDetailsRow.getGranularScope2FileName(),
                        nodeDetailsRow.getNodeStatus(),
                        nodeDetailsRow.isAudited(),
                        nodeDetailsRow.getAuditedAt(),
                        nodeDetailsRow.getUploadFileTransactionId(),
                        nodeDetailsRow.getAuditFileTransactionId(),
                        nodeDetailsRow.getScope1(),
                        nodeDetailsRow.getScope2(),
                        nodeDetailsRow.getTotalScope1AndScope2(),
                        nodeDetailsRow.getNodeDepth(),
                        nodeDetailsRow.getAuditorId(),
                        nodeDetailsRow.getEValue(),
                        nodeDetailsRow.getScope3(),
                        nodeDetailsRow.getProductionVolume()
                )
        );
    }


    private Mono<BigDecimal> bigDecimalFromScope(String value) {
        value = value.trim().replace(',', '.');
        return Mono.just(new BigDecimal(value));
    }

    private Mono<NetworkTreeDto> rebuildNetworkTreeCacheAndReturnTree(String networkId) {
        return bcNetworkDaoService.findById(networkId)
                .switchIfEmpty(Mono.error(new NotFoundException("Network not found: " + networkId)))
                .flatMap(net ->
                        bcNodeDaoService.findAllByNetworkIdWithCompanyName(networkId)
                                .collectList()
                                .flatMap(rows -> {
                                    var byId = rows.stream()
                                            .collect(Collectors.toMap(NodeWithCompanyNameRow::getId, r -> r));
                                    var byParent = rows.stream()
                                            .collect(Collectors.groupingBy(r -> r.getParentId() == null ? "__ROOT__" : r.getParentId()));

                                    // pick root: bc_network.root_id or first node with parent_id null
                                    String rootId = net.getRootId();
                                    if (rootId == null) {
                                        var roots = byParent.getOrDefault("__ROOT__" , List.of());
                                        if (roots.isEmpty()) {
                                            return Mono.error(new BadRequestException("No root node found for network " + networkId));
                                        }
                                        rootId = roots.getFirst().getId();
                                    }
                                    var root = byId.get(rootId);
                                    if (root == null) {
                                        return Mono.error(new BadRequestException("Root id not in this network: " + rootId));
                                    }

                                    NetworkTreeDto tree = toTree(root, byParent);
                                    tree.setNetworkId(networkId);

                                    // persist cache as JSON
                                    String json;
                                    try {
                                        json = objectMapper.writeValueAsString(tree);
                                    } catch (Exception e) {
                                        return Mono.error(new RuntimeException("Error serializing tree", e));
                                    }

                                    BcNetworkTreeCache cache = BcNetworkTreeCache.builder()
                                            .networkId(networkId)
                                            .treeJson(json)
                                            .build();

                                    return bcNetworkTreeCacheDaoService.save(cache).thenReturn(tree);
                                })
                );
    }

    @NotNull
    private Mono<Void> saveChildren(String networkId, NetworkTreeDto dto, BcNode savedParent) {
        List<NetworkTreeDto> children = dto.getChildren() != null ? dto.getChildren() : List.of();
        if (children.isEmpty()) return Mono.empty();

        final String parentIdForChildren = savedParent.getId();
        final int parentDepthForChildren = savedParent.getNodeDepth();

        return Flux.fromIterable(children)
                .concatMap(child ->
                        // per i figli, imponiamo il parentId = id appena creato
                        updateNodeRecursively(networkId, child, parentIdForChildren, parentDepthForChildren))
                .then();
    }

    private Mono<Void> deleteNodes(List<String> removed) {
        if (removed == null || removed.isEmpty()) return Mono.empty();

        return Flux.fromIterable(removed)
                .flatMap(id -> bcNodeDaoService.findById(id)
                        .switchIfEmpty(Mono.error(new BadRequestException("Node not found: " + id)))
                        .flatMap(this::deleteRecursively)
                )
                .then();
    }

    private Mono<Void> deleteRecursively(BcNode node) {
        return bcNodeDaoService.findByParentId(node.getId())
                .flatMap(this::deleteRecursively)
                .then(bcNodeDaoService.delete(node))
                .then();
    }

    private Mono<Void> refuseNode(BcNode node) {
        node.setNodeStatus(NodeStatus.REFUSED);
        node.setDeleted(true);
        node.setDeletedAt(LocalDateTime.now());
        return bcNodeDaoService.save(node)
                .then(bcNodeDaoService.findByParentId(node.getId())
                        .flatMap(child -> bcNodeDaoService.findById(child.getId())
                                .flatMap(this::refuseChildren)
                        ).collectList().then());
    }

    private Mono<Void> updateTree(String networkId, NetworkTreeDto tree) {
        return updateNodeRecursively(networkId, tree, null, 0);
    }

    private Mono<Void> updateNodeRecursively(String networkId,
                                             NetworkTreeDto dto,
                                             @Nullable String parentId,
                                             int knownParentDepth
    ) {

        if (dto.getCompanyId() == null || dto.getCompanyId().isBlank()) {
            return Mono.error(new BadRequestException("companyId is required for new nodes"));
        }

        if (dto.getId() != null) {
            return bcNodeDaoService.findById(dto.getId())
                    .flatMap(node -> {
                        boolean parentChanged = !Objects.equals(node.getParentId(), parentId);
                        boolean depthChanged = !Objects.equals(node.getNodeDepth(), knownParentDepth + 1);

                        if (parentChanged || depthChanged) {
                            node.setLastComputeChange(LocalDateTime.now());
                        }

                        node.setNodeDepth(knownParentDepth + 1);
                        node.setParentId(parentId);

                        return bcNodeDaoService.save(node)
                                .then(saveChildren(networkId, dto, node));
                    });
        } else {
            BcNode node = BcNode.builder()
                    .networkId(networkId)
                    .parentId(parentId)
                    .companyId(dto.getCompanyId())
                    .nodeDepth(knownParentDepth + 1)
                    .nodeStatus(NodeStatus.INVITED)
                    .audited(false)
                    .build();

            return bcNodeDaoService.save(node).flatMap(savedNode -> saveChildren(networkId, dto, savedNode));
        }
    }

    private Mono<Void> sanityCheckTree(String networkId, String adminId) {
        return bcNetworkDaoService.sanityCheckTree(networkId, adminId)
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new BadRequestException(
                                "Inconsistent network data: as an admin, you must be part of the network. " +
                                        "Also ensure that each company appears only once within the network."
                        ));
                    }
                    return Mono.empty();
                });
    }


    private Mono<TransactionOperationWrapper<NetworkTreeDto>> createNetworkTree(String networkId, NetworkTreeDto root) {
        return RequestContext.getCompanyId()
                .flatMap(companyId ->
                        bcNetworkDaoService.findById(networkId)
                                .flatMap(network ->
                                        traverseAndCollect(root, null, 1, networkId, companyId, LocalDateTime.now())
                                                .collectList()
                                                .flatMap(savedNodes -> {
                                                    if (savedNodes.isEmpty()) {
                                                        return Mono.error(new BadRequestException("Tree was empty after save"));
                                                    }
                                                    network.setRootId(savedNodes.getFirst().getId());
                                                    network.setMembersCount((long) savedNodes.size());
                                                    return bcNetworkDaoService.save(network).
                                                            then(bcNetworkTreeCacheDaoService.save(buildNetworkTreeCache(networkId, root)))
                                                            .then(bLedgerNetworkService.createBcBLedgerMetadata(networkId))
                                                            .flatMap(metadata -> transactionService.createTransaction(TransactionType.CREATE_NETWORK,companyId, null, metadata.getId(), networkId))
                                                            .map(tx -> new TransactionOperationWrapper<>(tx.getId(), root));
                                                })
                                )
                );
    }



}

