package it.bocconi.bledger.feature.wallet.service;

import it.bocconi.bledger.blockchain.config.Web3jConfig;
import it.bocconi.bledger.blockchain.service.BlockChainService;
import it.bocconi.bledger.blockchain.util.safe.SafeL2;
import it.bocconi.bledger.blockchain.util.safe.SafeProxyFactory;
import it.bocconi.bledger.blockchain.util.Web3Utils;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.feature.transaction.util.TransactionUtils;
import it.bocconi.bledger.feature.wallet.dao.service.BcWalletDaoService;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final BcWalletDaoService bcWalletDaoService;
    private final Web3jConfig web3jConfig;
    private final BlockChainService blockChainService;
    private final Credentials adminCredentials;
    private final BcTransactionDaoService bcTransactionDaoService;
    private final TransactionService transactionService;


    @Transactional
    public Mono<BcWallet> getOrCreateAdminWallet() {
        return bcWalletDaoService.findByType(WalletType.PLATFORM)
                .switchIfEmpty(createAdminWallet());
    }

    private Mono<BcWallet> createAdminWallet() {
        return createWallet(WalletType.PLATFORM);
    }

    @Transactional
    public Mono<BcWallet> createWallet(WalletType walletType) {
        BigInteger nonce = Web3Utils.generateNonce();
        String walletAddress = generateSafeWalletAddress(nonce);

        BcWallet wallet = BcWallet
                .builder()
                .salt(nonce)
                .walletAddress(walletAddress)
                .walletType(walletType)
                .toDeploy(false)
                .isWhitelisted(walletType == WalletType.PLATFORM)
                .build();


        return bcWalletDaoService.save(wallet)
                .flatMap(savedWallet -> switchSyncAsyncDeploy(walletType, savedWallet)
                        .flatMap(deployed -> {
                            if (!deployed) {
                                if (walletType == WalletType.PLATFORM) {
                                    log.error("Error deploying wallet");
                                } else {
                                    log.info("Smart contract will be deployed by transactions job");
                                }
                                savedWallet.setToDeploy(true);
                                return bcWalletDaoService.save(savedWallet);
                            }
                            return Mono.just(savedWallet);
                        }));

    }

    private Mono<Boolean> switchSyncAsyncDeploy(WalletType walletType, BcWallet savedWallet) {
        if(walletType == WalletType.PLATFORM) {
            return deployWallet(savedWallet.getId(), null);
        } else {
            return getOrCreateAdminWallet()
                    .flatMap(adminWallet -> transactionService.createTransaction(TransactionType.DEPLOY_WALLET, adminWallet.getId(), savedWallet.getWalletAddress(), savedWallet.getId(), null, null)
                            .thenReturn(false));
        }
    }


    public String generateSafeWalletAddress(BigInteger nonce) {
        SafeProxyFactory factory = blockChainService.getReadOnlySafeProxyFactory();
        String initializer = computeSafeInitializer();
        byte[] proxyCreationCode = factory.staticProxyCreationCode();
        log.debug("Proxy creation code: {}", Numeric.toHexString(proxyCreationCode));
        byte[] hashedInitializer = Hash.sha3(Numeric.hexStringToByteArray(initializer));
        log.debug("Hashed initializer: {}", Numeric.toHexString(hashedInitializer));
        String saltString = Hash.sha3(String.format("0x%s%s", Numeric.toHexStringNoPrefix(hashedInitializer), TypeEncoder.encode(new Uint(nonce))));
        log.debug("Salt: {}", saltString);
        byte[] salt = Numeric.hexStringToByteArray(saltString);
        String gnosisSafeL2 = web3jConfig.getGnosisSafeL2();
        String initData = Numeric.toHexString(proxyCreationCode) + TypeEncoder.encode(new Address(gnosisSafeL2));
        log.debug("Init data: {}", initData);
        byte[] initDataBytes = Numeric.hexStringToByteArray(initData);
        return ContractUtils.generateCreate2ContractAddress(factory.getContractAddress(), salt, initDataBytes);
    }

    private String computeSafeInitializer() {
        SafeL2 safe = blockChainService.getReadOnlySafeL2();
        String zeroAddress = Address.DEFAULT.toString();
        String fallbackAddress = web3jConfig.getSafeFallbackAddress();
        String ownerAddress = adminCredentials.getAddress();
        byte[] bytes = new byte[0];
        return safe.setup(List.of(ownerAddress), BigInteger.ONE, zeroAddress, bytes, fallbackAddress, zeroAddress, BigInteger.ZERO, zeroAddress).encodeFunctionCall();
    }


    @Transactional
    public Mono<Boolean> deployWalletByTransaction(String transactionId){
        return bcTransactionDaoService.findById(transactionId)
                .flatMap(tx -> deployWallet(tx.getPayload(), tx));
    }

    @Transactional
    public Mono<Boolean> deployWallet(String walletId, BcTransaction transaction) {
        // Already deployed? just persist the flag and return true

        return bcWalletDaoService.findById(walletId)
                .flatMap(wallet -> {
                    if (Boolean.TRUE.equals(wallet.getDeployed())) {
                        wallet.setToDeploy(false);
                        return bcWalletDaoService.save(wallet).thenReturn(true);
                    }

                    return blockChainService.getSafeProxyFactory(adminCredentials)
                            .flatMap(safeProxyFactory -> {
                                long operationStart = System.currentTimeMillis();

                                BigInteger nonce = wallet.getSalt();
                                String expectedAddress = wallet.getWalletAddress();
                                String gnosisSafeL2 = web3jConfig.getGnosisSafeL2();

                                log.info("Creating Gnosis Safe, nonce {}, expected address {}", nonce, expectedAddress);

                                byte[] initializer = Numeric.hexStringToByteArray(computeSafeInitializer());

                                Mono<TransactionReceipt> receiptMono =
                                        Mono.fromFuture(
                                                safeProxyFactory.createProxyWithNonce(gnosisSafeL2, initializer, nonce).sendAsync()
                                        );

                                return receiptMono
                                        .flatMap(tx -> {
                                            long operationEnd = System.currentTimeMillis();

                                            var creationEvents = SafeProxyFactory.getProxyCreationEvents(tx);
                                            if (creationEvents == null || creationEvents.isEmpty()) {
                                                log.error("Error deploying wallet {}, no creation event", wallet.getId());
                                                return Mono.just(false);
                                            }

                                            String createdAddress = creationEvents.getFirst().proxy;
                                            if (!createdAddress.equalsIgnoreCase(expectedAddress)) {
                                                log.error(
                                                        "Error deploying wallet {}, address mismatch expected {} got {}",
                                                        wallet.getId(), expectedAddress, createdAddress
                                                );
                                                return Mono.just(false);
                                            }

                                            log.info("Wallet {} deployed in {} ms", wallet.getId(), (operationEnd - operationStart));

                                            wallet.setToDeploy(false);
                                            wallet.setDeployed(true);

                                            return bcWalletDaoService.save(wallet)
                                                    .flatMap(saved -> {
                                                        BcTransaction bcTransaction = transaction == null ? new BcTransaction() : transaction;
                                                        return compileWalletCreationTransaction(bcTransaction, tx, saved.getId()).thenReturn(true);
                                                    });
                                        })
                                        .switchIfEmpty(Mono.fromCallable(() -> {
                                            log.error("Error deploying wallet {}, no receipt", wallet.getId());
                                            return false;
                                        }))
                                        .onErrorResume(err -> {
                                            log.error("Error deploying wallet {}", wallet.getId(), err);
                                            return Mono.just(false);
                                        });
                            });
                });
    }


    private Mono<BcTransaction> compileWalletCreationTransaction(BcTransaction transaction, TransactionReceipt tx, String walletId){
        BcTransaction bcTransaction = TransactionUtils.setTransactionFields(transaction, tx);
        bcTransaction.setCreatorId(walletId);
        bcTransaction.setTransactionType(TransactionType.DEPLOY_WALLET);
        bcTransaction.setTransactionData("{\"walletId\": \"" + walletId + "\"}");
        return bcTransactionDaoService.save(bcTransaction);
    }

}
