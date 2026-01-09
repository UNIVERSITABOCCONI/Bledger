package it.bocconi.bledger.feature.file.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.exception.BadRequestException;
import it.bocconi.bledger.exception.ForbiddenException;
import it.bocconi.bledger.exception.NotFoundException;
import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.enums.FileStatus;
import it.bocconi.bledger.feature.file.enums.FileType;
import it.bocconi.bledger.feature.file.dao.service.BcFileBinaryDaoService;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;
import it.bocconi.bledger.feature.file.util.CsvUtils;
import it.bocconi.bledger.feature.file.util.ExcelUtil;
import it.bocconi.bledger.feature.file.util.FileUtils;
import it.bocconi.bledger.feature.file.util.ZipUtil;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkDaoService;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.feature.network.entity.BcNodeMetadata;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import it.bocconi.bledger.feature.network.dao.service.BcNodeMetadataDaoService;
import it.bocconi.bledger.feature.network.dao.service.BcNodeDaoService;
import it.bocconi.bledger.feature.network.mapper.NetworkMapper;
import it.bocconi.bledger.feature.network.router.dto.NodeMetadataDto;
import it.bocconi.bledger.feature.smartcontract.service.BLedgerNetworkService;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.router.dto.TransactionOperationWrapper;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.bocconi.bledger.feature.network.entity.IBcNode;

@Service
@AllArgsConstructor
public class FileService {

    private final BcFileDaoService bcFileDaoService;
    private final BcFileBinaryDaoService bcFileBinaryDaoService;
    private final BcNodeDaoService bcNodeDaoService;
    private final BcNodeMetadataDaoService bcNodeMetadataDaoService;
    private final ObjectMapper objectMapper;
    private final ResourceService resourceService;
    private final NetworkMapper networkMapper;
    private final BLedgerNetworkService bLedgerNetworkService;
    private final TransactionService transactionService;
    private final BcNetworkDaoService bcNetworkDaoService;
    private static final List<String> GRANULAR_DATA_HEADERS = List.of("name", "category", "source_of_emission", "unit_of_measure", "quantity");

    //Access points
    @Transactional
    public Mono<String> uploadFiles(
            String networkId,
            Mono<String> companyIdMono,
            FilePart scope1File,
            FilePart scope2File,
            FilePart globalFile) {

        return companyIdMono.flatMap(companyId ->
                getZippedSaveFileMono(scope1File, scope2File, globalFile)
                        .flatMap(tuple -> updateNodeAndGetResult(networkId, companyId, tuple))
        );
    }

    @Transactional
    public Mono<TransactionOperationWrapper<String>> confirmFileDataAndExtractMetadata(String networkId, String companyId) {
        return bcNodeDaoService.findByNetworkIdAndCompanyIdWithCompanyName(networkId, companyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Node not found")))
                .flatMap(node -> getZippedFindFilesByIdMono(node)
                        .switchIfEmpty(Mono.error(new NotFoundException("Node files not found")))
                        .flatMap(tuple -> extractMetadataFromFiles(node, tuple)
                                .flatMap(FileService::validateData)
                                .flatMap(extractedTuple -> bcNetworkDaoService.incrementUploadCount(networkId)
                                        .then(bLedgerNetworkService.createBcBLedgerMetadata(networkId)
                                                .flatMap(metadata -> transactionService.createTransaction(TransactionType.UPLOAD_FILE, companyId, null, metadata.getId(), networkId, node.getId()))
                                                .flatMap(tx -> {
                                                    node.setUploadFileTransactionId(tx.getId());
                                                    node.setNodeStatus(NodeStatus.FILE_UPLOADED);
                                                    node.setScope1(extractedTuple.getT2().scope1());
                                                    node.setScope2(extractedTuple.getT2().scope2());
                                                    node.setTotalScope1AndScope2(extractedTuple.getT2().total());
                                                    return bcNodeDaoService.save(networkMapper.toEntity(node)).thenReturn(tx);
                                                })
                                                .map(tx -> new TransactionOperationWrapper<>(tx.getId(), extractedTuple.getT1())))
                        )));
    }

    @Transactional
    public Mono<byte[]> generateExportZip(String networkId, String companyId) {
        return bcNodeDaoService.findByNetworkIdAndCompanyId(networkId, companyId)
                .switchIfEmpty(Mono.error(new BadRequestException("You are not part of this network")))
                .flatMap(currentNode ->
                        bcNodeDaoService.findChildrenCompletionPercentage(currentNode.getId())
                                .flatMap(percentage -> {
                                    if (percentage < 100) {
                                        return Mono.error(new BadRequestException(
                                                "Some suppliers have not yet uploaded their required documents. Current completion: " + percentage + "%"
                                        ));
                                    }
                                    currentNode.setLastExport(LocalDateTime.now());
                                    return bcNodeDaoService.save(currentNode);
                                })

                ).flatMap(currentNode ->
                        bcNodeDaoService.findChildren(currentNode.getId())
                                .flatMap(node -> exportNodeZip(node).map(zip -> Map.entry(node, zip)))
                                .collectList()
                                .flatMap(entries ->
                                        ExcelUtil.createSummaryExcelMono(
                                                entries.stream()
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toList()),
                                                currentNode
                                        ).flatMap(excelBytes -> ZipUtil.exportZipMono(entries, excelBytes))
                                ));
    }

    @Transactional(readOnly = true)
    public Mono<byte[]> downloadAll(String nodeId) {
        return bcNodeDaoService.findById(nodeId)
                .flatMap(node -> RequestContext.getCompanyId()
                        .flatMap(requestCompanyId -> {
                            if (!requestCompanyId.equals(node.getCompanyId())) {
                                return Mono.error(new IllegalStateException("Not authorized to download this file"));
                            }
                            return exportNodeZip(node);
                        }));
    }

    @Transactional
    public Mono<Void> deleteFileData(String networkId, String companyId) {
        return bcNodeDaoService.findByNetworkIdAndCompanyIdWithCompanyName(networkId, companyId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Node not found")))
                .flatMap(nodeRow -> {
                    final String nodeId = nodeRow.getId();

                    // fetch files (if any). Some ids might be null; handle defensively.
                    Mono<BcFile> globalMono = safeFindFile(bcFileDaoService, nodeRow.getScopeFileId());
                    Mono<BcFile> scope1Mono = safeFindFile(bcFileDaoService, nodeRow.getGranularScope1FileId());
                    Mono<BcFile> scope2Mono = safeFindFile(bcFileDaoService, nodeRow.getGranularScope2FileId());

                    return Mono.zip(globalMono, scope1Mono, scope2Mono)
                            .flatMap(tuple -> deleteFilesAndData(nodeRow, tuple, nodeId));
                })
                .then();
    }


    @Transactional(readOnly = true)
    public Mono<Tuple2<BcFile, BcFileBinary>> downloadFile(String nodeId, FileType type) {
        if (type == FileType.PROFILE_IMAGE) {
            return Mono.error(new IllegalArgumentException("PROFILE_IMAGE is not allowed for this endpoint"));
        }
        return RequestContext.getCompanyId()
                .flatMap(requestCompanyId ->
                        getFileByNodeAndType(nodeId, type, requestCompanyId)
                );
    }

    @NotNull
    private Mono<Tuple2<BcFile, BcFileBinary>> getFileByNodeAndType(String nodeId, FileType type, String requestCompanyId) {
        return bcNodeDaoService.findById(nodeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Node not found: " + nodeId)))
                .flatMap(node -> {
                    // authorization: only node owner or its auditor
                    if (!(requestCompanyId.equals(node.getCompanyId()) ||
                            requestCompanyId.equals(node.getAuditorId()))) {
                        return Mono.error(new ForbiddenException("Not authorized to download this file"));
                    }

                    // resolve fileId
                    String fileId = switch (type) {
                        case GLOBAL_SCOPE_FILE -> node.getScopeFileId();
                        case GRANULAR_SCOPE_1 -> node.getGranularScope1FileId();
                        case GRANULAR_SCOPE_2 -> node.getGranularScope2FileId();
                        default -> null;
                    };

                    if (fileId == null) {
                        return Mono.error(new IllegalStateException("No file uploaded for type " + type));
                    }

                    // fetch file and binary
                    return bcFileDaoService.findById(fileId)
                            .flatMap(file ->
                                    bcFileBinaryDaoService.findById(file.getBinaryId())
                                            .map(binary -> Tuples.of(file, binary))
                            );
                });
    }

    public List<NodeMetadataDto> getMetadataDtoFromFile(BcFile file){
        try {
            List<Map<String, String>> records = objectMapper.readValue(file.getExtractedData(), new TypeReference<>() {});
            List<NodeMetadataDto> metadataList = new ArrayList<>();
            for(Map<String, String> map : records) {
                metadataList.add(new NodeMetadataDto(
                        map.get("scope_1_2_component"),
                        map.get("total_co2e"),
                        map.get("unit")
                ));
            }
            return metadataList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



    //private methods

    private Mono<String> extractJsonFromCsv(FilePart part, FileType fileType) {
        return FileUtils.getBytesFromFilePartMono(part)
                .flatMap(bytes -> {
                    String csv = new String(bytes, StandardCharsets.UTF_8);
                    return fileType == FileType.GLOBAL_SCOPE_FILE ? CsvUtils.convertGlobalFileToJsonMono(csv) : CsvUtils.convertCsvToJsonMono(csv, GRANULAR_DATA_HEADERS);
                });
    }

    private Mono<FileSaveResult> saveFileAndExtractData(FilePart part, FileType fileType) {
        return extractJsonFromCsv(
                part,
                fileType
        )
                .flatMap(json -> saveFileAndBinary(part, fileType, json)
                        .map(id -> new FileSaveResult(id, json))
                );
    }

    private Mono<String> saveFileAndBinary(FilePart part, FileType fileType, String json) {
        return FileUtils.getBytesFromFilePartMono(part)
                .flatMap(fileBytes -> {
                    BcFileBinary binary = BcFileBinary
                            .builder()
                            .fileBinary(fileBytes)
                            .build();

                    return bcFileBinaryDaoService.save(binary)
                            .flatMap(savedBinary ->
                                    FileUtils.hashFileMono(fileBytes) // use Keccak-256
                                            .flatMap(keccak256 -> {
                                                BcFile file = BcFile.builder()
                                                        .fileName(part.filename())
                                                        .mimeType(part.headers().getContentType().toString())
                                                        .size((long) fileBytes.length)
                                                        .keccak256(keccak256)
                                                        .fileStatus(FileStatus.PENDING)
                                                        .binaryId(savedBinary.getId())
                                                        .type(fileType)
                                                        .extractedData(json)
                                                        .build();

                                                return bcFileDaoService.save(file).map(BcFile::getId);
                                            })
                            );
                });
    }




    private GlobalDataResult parseGlobalMetadata(String nodeId, String json) {
        try {
            List<Map<String, String>> records = objectMapper.readValue(json, new TypeReference<>() {});
            String scope1 = null;
            String scope2 = null;
            String totalScope = null;

            List<BcNodeMetadata> metadataList = new ArrayList<>();

            for(Map<String, String> map : records) {
                if (map.get("scope_1_2_component").equals("scope_1_total_emission")) {
                    scope1 = map.get("total_co2e");
                } else if (map.get("scope_1_2_component").equals("scope_2_total_emission")) {
                    scope2 = map.get("total_co2e");
                } else if (map.get("scope_1_2_component").equals("total_emission")) {
                    totalScope = map.get("total_co2e");
                }

                metadataList.add(BcNodeMetadata.builder()
                        .nodeId(nodeId)
                        .metadataType(NodeMetadataType.GLOBAL_SCOPE)
                        .metadataKey(map.get("scope_1_2_component"))
                        .metadataValue(map.get("total_co2e"))
                        .unitOfMeasure(map.get("unit"))
                        .build());

            }

            return new GlobalDataResult(scope1, scope2, totalScope, metadataList);
        } catch (Exception e) {
            throw new RuntimeException("Invalid global xbrl-metadata JSON", e);
        }
    }



    private List<BcNodeMetadata> parseGranularMetadata(String nodeId, String json, NodeMetadataType type) {
        try {
            List<Map<String, Object>> records = objectMapper.readValue(json, new TypeReference<>() {});
            return records.stream()
                    .map(map -> BcNodeMetadata.builder()
                            .nodeId(nodeId)
                            .metadataType(type)
                            .metadataKey((String) map.get("name"))
                            .metadataValue(String.valueOf(map.get("quantity")))
                            .category((String) map.get("category"))
                            .sourceOfEmission((String) map.get("source_of_emission"))
                            .unitOfMeasure((String) map.get("unit_of_measure"))
                            .build()
                    ).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Invalid granular xbrl-metadata JSON", e);
        }
    }


    @NotNull
    private Mono<byte[]> exportNodeZip(IBcNode node) {
        return Mono.zip(
                bcFileBinaryDaoService.findByFileId(node.getGranularScope1FileId())
                        .defaultIfEmpty(emptyBinary()),
                bcFileBinaryDaoService.findByFileId(node.getGranularScope2FileId())
                        .defaultIfEmpty(emptyBinary()),
                bcFileBinaryDaoService.findByFileId(node.getScopeFileId())
                        .defaultIfEmpty(emptyBinary()),
                resourceService.getMetadataJson()
        ).flatMap(tuple -> {
            byte[] scope1 = tuple.getT1().getFileBinary();
            byte[] scope2 = tuple.getT2().getFileBinary();
            byte[] global = tuple.getT3().getFileBinary();
            byte[] metadata;
            try {
                metadata = tuple.getT4().getContentAsByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return ZipUtil.internalZipMono(scope1, scope2, global, metadata);
        });
    }


    @NotNull
    private Mono<Tuple3<FileSaveResult, FileSaveResult, FileSaveResult>> getZippedSaveFileMono(FilePart scope1File,
                                                                               FilePart scope2File,
                                                                               FilePart globalFile) {

        return Mono.zip(saveFileAndExtractData(scope1File, FileType.GRANULAR_SCOPE_1),
                saveFileAndExtractData(scope2File, FileType.GRANULAR_SCOPE_2),
                saveFileAndExtractData(globalFile, FileType.GLOBAL_SCOPE_FILE));
    }


    @NotNull
    private Mono<String> updateNodeAndGetResult(String networkId, String companyId, Tuple3<FileSaveResult, FileSaveResult, FileSaveResult> tuple) {
        String scope1Id = tuple.getT1().id();
        String scope2Id = tuple.getT2().id();
        String globalId = tuple.getT3().id();
        String globalJson = tuple.getT3().json();

        return bcNodeDaoService.findByNetworkIdAndCompanyIdWithCompanyName(networkId, companyId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Node not found")))
                .flatMap(node -> {
                    BcNode nodeEntity = networkMapper.toEntity(node);
                    nodeEntity.setGranularScope1FileId(scope1Id);
                    nodeEntity.setGranularScope2FileId(scope2Id);
                    nodeEntity.setScopeFileId(globalId);
                    return bcNodeDaoService.save(nodeEntity).thenReturn(globalJson);
                });
    }



    @NotNull
    private Mono<Tuple3<BcFile, BcFile, BcFile>> getZippedFindFilesByIdMono(NodeWithCompanyNameRow node) {
        return Mono.zip(
                bcFileDaoService.findById(node.getScopeFileId()),
                bcFileDaoService.findById(node.getGranularScope1FileId()),
                bcFileDaoService.findById(node.getGranularScope2FileId())
        );
    }

    @NotNull
    private Mono<Tuple2<String, GlobalDataResult>> extractMetadataFromFiles(
            NodeWithCompanyNameRow node,
            Tuple3<BcFile, BcFile, BcFile> tuple
    ) {
        String nodeId = node.getId();
        BcFile global = tuple.getT1();
        BcFile scope1 = tuple.getT2();
        BcFile scope2 = tuple.getT3();

        global.setFileStatus(FileStatus.CONFIRMED);
        scope1.setFileStatus(FileStatus.CONFIRMED);
        scope2.setFileStatus(FileStatus.CONFIRMED);

        GlobalDataResult globalMetadata = parseGlobalMetadata(nodeId, global.getExtractedData());
        List<BcNodeMetadata> scope1Metadata =
                parseGranularMetadata(nodeId, scope1.getExtractedData(), NodeMetadataType.SCOPE1_GRANULAR);
        List<BcNodeMetadata> scope2Metadata =
                parseGranularMetadata(nodeId, scope2.getExtractedData(), NodeMetadataType.SCOPE2_GRANULAR);

        List<BcNodeMetadata> allMetadata = new ArrayList<>();
        allMetadata.addAll(globalMetadata.metadata());
        allMetadata.addAll(scope1Metadata);
        allMetadata.addAll(scope2Metadata);

        BcNode nodeEntity = networkMapper.toEntity(node);
        nodeEntity.setNodeStatus(NodeStatus.FILE_UPLOADED);
        nodeEntity.setScope1(globalMetadata.scope1());
        nodeEntity.setScope2(globalMetadata.scope2());
        nodeEntity.setTotalScope1AndScope2(globalMetadata.total());

        return Mono.when(
                bcFileDaoService.saveAll(List.of(global, scope1, scope2)),
                bcNodeDaoService.save(nodeEntity),
                bcNodeMetadataDaoService.saveAll(allMetadata)
        ).thenReturn(Tuples.of(global.getExtractedData(), globalMetadata));
    }

    private static BcFileBinary emptyBinary() {
        return BcFileBinary.builder().fileBinary(new byte[0]).build();
    }

    @NotNull
    private Mono<Void> deleteFilesAndData(NodeWithCompanyNameRow nodeRow, Tuple3<BcFile, BcFile, BcFile> tuple, String nodeId) {
        BcFile global = tuple.getT1();
        BcFile scope1 = tuple.getT2();
        BcFile scope2 = tuple.getT3();

        Mono<Void> delGlobal = softDeleteFileAndBinary(global);
        Mono<Void> delScope1 = softDeleteFileAndBinary(scope1);
        Mono<Void> delScope2 = softDeleteFileAndBinary(scope2);

        Mono<Integer> delMeta = bcNodeMetadataDaoService.deleteAllByNodeId(nodeId);

        BcNode nodeEntity = networkMapper.toEntity(nodeRow);
        nodeEntity.setScopeFileId(null);
        nodeEntity.setGranularScope1FileId(null);
        nodeEntity.setGranularScope2FileId(null);
        nodeEntity.setScope1(null);
        nodeEntity.setScope2(null);
        nodeEntity.setTotalScope1AndScope2(null);


        Mono<BcNode> saveNode = bcNodeDaoService.save(nodeEntity);

        return Mono.when(delGlobal, delScope1, delScope2, delMeta.then(), saveNode.then());
    }

    private Mono<BcFile> safeFindFile(BcFileDaoService fileDao, String fileId) {
        return fileId == null ? Mono.justOrEmpty((BcFile) null) : fileDao.findById(fileId).onErrorResume(e -> Mono.empty());
    }

    private Mono<Void> softDeleteFileAndBinary(BcFile file) {
        if (file == null) return Mono.empty();

        file.setDeleted(true);
        Mono<Void> markFile = bcFileDaoService.save(file).then();

        Mono<Void> markBinary = Mono.empty();
        if (file.getBinaryId() != null) {
            markBinary = bcFileBinaryDaoService.findById(file.getBinaryId())
                    .flatMap(bin -> {
                        bin.setDeleted(true);
                        return bcFileBinaryDaoService.save(bin);
                    })
                    .then();
        }
        return Mono.when(markFile, markBinary);
    }




    //utility records
    private record GlobalDataResult(String scope1, String scope2, String total, List<BcNodeMetadata> metadata) {}

    private record FileSaveResult(String id, String json) {}

    @NotNull
    private static Mono<Tuple2<String, GlobalDataResult>> validateData(Tuple2<String, GlobalDataResult> extractedDataToValidate) {
        GlobalDataResult globalDataResult = extractedDataToValidate.getT2();

        for(BcNodeMetadata metadata : globalDataResult.metadata()) {
            if (!isValidBigDecimal(metadata.getMetadataValue())) {
                return Mono.error(new BadRequestException(String.format("The value in the field %s must be a valid number. Please use a period (\".\") as the decimal separator, delete this upload, and upload a corrected file", metadata.getMetadataKey())));
            }
        }

        if(globalDataResult.scope1() == null || globalDataResult.scope2() == null) {
            return Mono.error(new BadRequestException("Total Scope 1 and Total Scope 2 are mandatory"));
        }

        return Mono.just(extractedDataToValidate);
    }

    private static boolean isValidBigDecimal(String value){
        if (value == null) return false;
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
