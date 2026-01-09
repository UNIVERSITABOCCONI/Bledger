package it.bocconi.bledger.feature.smartcontract.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.blockchain.service.BlockChainService;
import it.bocconi.bledger.blockchain.smartcontract.generated.BLedgerNetwork;
import it.bocconi.bledger.blockchain.util.Web3Utils;
import it.bocconi.bledger.exception.NotFoundException;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;
import it.bocconi.bledger.feature.file.util.FileUtils;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkAuditorDaoService;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkDaoService;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkTreeCacheDaoService;
import it.bocconi.bledger.feature.network.entity.BcNetwork;
import it.bocconi.bledger.feature.network.router.dto.NetworkTreeDto;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcBLedgerNetworkDaoService;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcBLedgerNetworkMetadata;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.entity.NetworkMetadataDto;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.smartcontract.utils.TreeHasher;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.util.TransactionUtils;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class BLedgerNetworkService {

    private final BcFileDaoService bcFileDaoService;
    private final BcNetworkDaoService bcNetworkDaoService;
    private final BcBLedgerNetworkDaoService bcBLedgerNetworkDaoService;
    private final BcNetworkTreeCacheDaoService bcNetworkTreeCacheDaoService;
    private final ObjectMapper objectMapper;
    private final BcTransactionDaoService bcTransactionDaoService;
    private final String baseUrl;
    private final BcSmartContractDaoService bcSmartContractDaoService;
    private final Credentials adminCredentials;
    private final BlockChainService blockChainService;
    private final WalletService walletService;
    private final BcCompanyDaoService bcCompanyDaoService;
    private final BcNetworkAuditorDaoService bcNetworkAuditorDaoService;
    private final Web3Utils web3Utils;


    public BLedgerNetworkService(BcFileDaoService bcFileDaoService,
                                 BcNetworkDaoService bcNetworkDaoService,
                                 BcBLedgerNetworkDaoService bcBLedgerNetworkDaoService,
                                 BcNetworkTreeCacheDaoService bcNetworkTreeCacheDaoService,
                                 ObjectMapper objectMapper,
                                 BcTransactionDaoService bcTransactionDaoService,
                                 @Value("${it.bocconi.bledger.smart-contract.bl-ledger-network.base-url}") String baseUrl, BcSmartContractDaoService bcSmartContractDaoService, Credentials adminCredentials, BlockChainService blockChainService, WalletService walletService, BcCompanyDaoService bcCompanyDaoService, BcNetworkAuditorDaoService bcNetworkAuditorDaoService, Web3Utils web3Utils) {
        this.bcFileDaoService = bcFileDaoService;
        this.bcNetworkDaoService = bcNetworkDaoService;
        this.bcBLedgerNetworkDaoService = bcBLedgerNetworkDaoService;
        this.bcNetworkTreeCacheDaoService = bcNetworkTreeCacheDaoService;
        this.objectMapper = objectMapper;
        this.bcTransactionDaoService = bcTransactionDaoService;
        this.baseUrl = baseUrl;
        this.bcSmartContractDaoService = bcSmartContractDaoService;
        this.adminCredentials = adminCredentials;
        this.blockChainService = blockChainService;
        this.walletService = walletService;
        this.bcCompanyDaoService = bcCompanyDaoService;
        this.bcNetworkAuditorDaoService = bcNetworkAuditorDaoService;
        this.web3Utils = web3Utils;
    }

    public Mono<BcBLedgerNetworkMetadata> createBcBLedgerMetadata(String networkId) {
        return bcNetworkTreeCacheDaoService.findByNetworkId(networkId)
                .flatMap(bcNetworkTreeCache ->
                        ReactiveUtils.scheduleOnBundleElasticMono(() ->
                                        objectMapper.readValue(bcNetworkTreeCache.getTreeJson(), NetworkTreeDto.class))
                                .flatMap(networkTreeDto ->
                                        computeNetworkMetadata(networkTreeDto)
                                                .flatMap(metadataDto ->
                                                        ReactiveUtils.scheduleOnBundleElasticMono(() ->
                                                                        objectMapper.writeValueAsString(metadataDto))
                                                                .flatMap(metadataJson ->
                                                                        FileUtils.hashFileMono(metadataJson.getBytes(StandardCharsets.UTF_8))
                                                                                .flatMap(keccak256 -> {
                                                                                    BcBLedgerNetworkMetadata metadata = BcBLedgerNetworkMetadata.builder()
                                                                                            .contentHash(keccak256)
                                                                                            .content(metadataJson)
                                                                                            .networkId(networkId)
                                                                                            .build();
                                                                                    return bcBLedgerNetworkDaoService.save(metadata);
                                                                                })
                                                                )
                                                )
                                )
                );
    }


    public Mono<NetworkMetadataDto> computeNetworkMetadata(NetworkTreeDto tree) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> TreeHasher.hashNodeUnordered(tree))
                .flatMap(hash -> bcNetworkDaoService.findById(tree.getNetworkId())
                        .flatMap(bcNetwork -> bcFileDaoService.findAllNetworkFiles(bcNetwork.getId())
                                .collectList()
                                .map(files -> new NetworkMetadataDto(
                                        bcNetwork.getId(),
                                        bcNetwork.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                                        bcNetwork.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                                        Instant.now(),
                                        hash,
                                        bcNetwork.getMembersCount(),
                                        bcNetwork.getUploadedCount(),
                                        bcNetwork.getAuditedCount(),
                                        files
                                ))));

    }

    @Transactional
    public Mono<Boolean> processMintTransaction(String transactionId) {
        return bcTransactionDaoService.findById(transactionId)
                .flatMap(tx -> Mono.zip(bcNetworkDaoService.findById(tx.getNetworkId()), bcBLedgerNetworkDaoService.findById(tx.getPayload()))
                        .flatMap(networkTuple -> {
                            var network = networkTuple.getT1();
                            var metadata = networkTuple.getT2();
                            String url = baseUrl + "/" + metadata.getContentHash();
                            return Mono.zip(bcSmartContractDaoService.findByType(SmartContractType.ERC721),
                                            walletService.getOrCreateAdminWallet(),
                                            bcCompanyDaoService.getCompanyWalletAddressById(network.getNetworkAdminId()),
                                            bcNetworkAuditorDaoService.findAuditorAddressedByNetworkId(network.getId()).collectList()
                                                    )
                                    .flatMap(tuple -> {
                                        BcSmartContract erc721 = tuple.getT1();
                                        BcWallet wallet = tuple.getT2();
                                        String to = tuple.getT3();
                                        List<String> auditors = tuple.getT4();
                                        return blockChainService.bLedgerNetwork(adminCredentials, erc721.getContractAddress())
                                                .flatMap(bLedgerNetwork -> {
                                                    String mintCall = bLedgerNetwork.safeMintWithAuditors(to, url, Numeric.hexStringToByteArray("0x" + metadata.getContentHash()), auditors).encodeFunctionCall();
                                                    return web3Utils.prepareSafeTransaction(wallet.getWalletAddress(), erc721.getContractAddress(), mintCall, false)
                                                            .flatMap(safeTransaction -> Mono.fromFuture(safeTransaction.sendAsync())
                                                                    .flatMap(transactionReceipt -> {
                                                                        TransactionUtils.setTransactionFields(tx, transactionReceipt);
                                                                        var tokenId = BLedgerNetwork.getTransferEvents(transactionReceipt).getFirst().tokenId;
                                                                        network.setTokenId(tokenId);
                                                                        tx.setTransactionData("{\"tokenId\": \"" + tokenId + "\"}");
                                                                        return Mono.zip(bcTransactionDaoService.save(tx), bcNetworkDaoService.save(network)).thenReturn(TransactionStatus.CONFIRMED.equals(tx.getStatus()));
                                                                    }));
                                                });

                                    });

                        }));
    }

    @Transactional
    public Mono<Boolean> processUpdateTransaction(String transactionId) {
        return bcTransactionDaoService.findById(transactionId)
                .flatMap(tx -> Mono.zip(bcNetworkDaoService.findById(tx.getNetworkId())
                                .switchIfEmpty(Mono.error(new NotFoundException("network not found"))),
                                bcBLedgerNetworkDaoService.findById(tx.getPayload())
                                        .switchIfEmpty(Mono.error(new NotFoundException("metadata not found")))
                        )
                        .flatMap(networkTuple -> {
                            var network = networkTuple.getT1();
                            var metadata = networkTuple.getT2();
                            String url = baseUrl + "/" + metadata.getContentHash();

                            Mono<String> walletAddressMono = (tx.getTransactionType() == TransactionType.UPLOAD_FILE || tx.getTransactionType() == TransactionType.REFUSED_INVITATION)  ? walletService.getOrCreateAdminWallet().map(BcWallet::getWalletAddress) : bcCompanyDaoService.getCompanyWalletAddressById(tx.getCreatorId());

                            return Mono.zip(bcSmartContractDaoService.findByType(SmartContractType.ERC721)
                                                    .switchIfEmpty(Mono.error(new NotFoundException("smartContract not found"))),
                                            walletAddressMono
                                                    .switchIfEmpty(Mono.error(new NotFoundException("creator wallet not found id:" + tx.getCreatorId())))
                                    )
                                    .flatMap(tuple -> {
                                        BcSmartContract erc721 = tuple.getT1();
                                        String walletAddress = tuple.getT2();
                                        return blockChainService.bLedgerNetwork(adminCredentials, erc721.getContractAddress())
                                                .flatMap(bLedgerNetwork -> {
                                                    String updateCall = bLedgerNetwork.updateTokenURI(network.getTokenId(), url, Numeric.hexStringToByteArray("0x" + metadata.getContentHash()), getReasonFromType(tx.getTransactionType())).encodeFunctionCall();
                                                    return web3Utils.prepareSafeTransaction(walletAddress, erc721.getContractAddress(), updateCall, false)
                                                            .flatMap(safeTransaction -> Mono.fromFuture(safeTransaction.sendAsync())
                                                                    .flatMap(transactionReceipt -> {
                                                                        TransactionUtils.setTransactionFields(tx, transactionReceipt);
                                                                        tx.setTransactionData("{\"tokenId\": \"" + network.getTokenId() + "\"}");
                                                                        return bcTransactionDaoService.save(tx).thenReturn(TransactionStatus.CONFIRMED.equals(tx.getStatus()));
                                                                    }));
                                                });

                                    });

                        }));
    }

    private BigInteger getReasonFromType(TransactionType transactionType) {
        return switch (transactionType) {
            case UPDATE_NETWORK -> BigInteger.ZERO;
            case UPLOAD_FILE -> BigInteger.ONE;
            case AUDIT -> BigInteger.TWO;
            case REFUSED_INVITATION -> BigInteger.valueOf(3);
            default -> throw new RuntimeException("Not an update transaction type");
        };
    }

    @Transactional
    public Mono<Boolean> processAddAuditors(String transactionId) {
        return bcTransactionDaoService.findById(transactionId)
                .flatMap(tx -> {
                    List<String> companyIds;
                    try {
                        companyIds = objectMapper.readValue(
                                tx.getPayload(),
                                new TypeReference<List<String>>() {}
                        );
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                    return Mono.zip(
                            bcNetworkDaoService.findById(tx.getNetworkId())
                                    .switchIfEmpty(Mono.error(new NotFoundException("network not found"))),
                            bcCompanyDaoService.getWalletAddressesByIds(companyIds).collectList(),
                            bcCompanyDaoService.getCompanyWalletAddressById(tx.getCreatorId()),
                            bcSmartContractDaoService.findByType(SmartContractType.ERC721)
                                    .switchIfEmpty(Mono.error(new NotFoundException("smartContract not found"))))
                                    .flatMap(tuple -> {
                                        BcNetwork network = tuple.getT1();
                                        List<String> newAuditorsAddresses = tuple.getT2();
                                        String creatorAddress = tuple.getT3();
                                        BcSmartContract erc721 = tuple.getT4();
                                        return blockChainService.bLedgerNetwork(adminCredentials, erc721.getContractAddress())
                                                .flatMap(bLedgerNetwork -> {
                                                    String updateCall = bLedgerNetwork.setAuditors(network.getTokenId(), newAuditorsAddresses, true).encodeFunctionCall();
                                                    return web3Utils.prepareSafeTransaction(creatorAddress, erc721.getContractAddress(), updateCall, false)
                                                            .flatMap(safeTransaction -> Mono.fromFuture(safeTransaction.sendAsync())
                                                                    .flatMap(transactionReceipt -> {
                                                                        TransactionUtils.setTransactionFields(tx, transactionReceipt);
                                                                        tx.setTransactionData("{\"tokenId\": \"" + network.getTokenId() + "\"}");
                                                                        return bcTransactionDaoService.save(tx).thenReturn(TransactionStatus.CONFIRMED.equals(tx.getStatus()));
                                                                    }));
                                                });
                                    });
                });
    }
}
