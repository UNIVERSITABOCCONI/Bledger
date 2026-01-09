package it.bocconi.bledger.blockchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.blockchain.smartcontract.generated.IdentityManager;
import it.bocconi.bledger.blockchain.util.Web3Utils;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.util.TransactionUtils;
import it.bocconi.bledger.feature.wallet.dao.service.BcWalletDaoService;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityManagerService {

    private final BcWalletDaoService bcWalletDaoService;
    private final BcSmartContractDaoService bcSmartContractDaoService;
    private final Credentials adminCredentials;
    private final BlockChainService blockChainService;
    private final WalletService walletService;
    private final Web3Utils web3Utils;
    private final BcTransactionDaoService bcTransactionDaoService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Mono<Boolean> whitelistWalletByTransaction(String transactionId) {
        return bcTransactionDaoService.findById(transactionId)
                .flatMap(tx -> {
                    String walletId;
                    boolean isAuditor;
                    try {
                        Map<String, String> payloadMap = objectMapper.readValue(
                                tx.getPayload(),
                                new TypeReference<>() {
                                }
                        );
                        walletId = payloadMap.get("walletId");
                        isAuditor = Boolean.parseBoolean(payloadMap.get("isAuditor"));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return whitelistWallet(walletId, tx, isAuditor);
                });
    }

    @Transactional
    public Mono<Boolean> whitelistWallet(String walletId, BcTransaction transaction, boolean auditor) {
        return bcWalletDaoService.findById(walletId)
                .flatMap(wallet -> whitelistAddress(wallet.getWalletAddress(), walletId, transaction, auditor ? BigInteger.TWO : BigInteger.ONE)
                        .flatMap(success -> {
                            wallet.setIsWhitelisted(success);
                            return bcWalletDaoService.save(wallet).thenReturn(success);
                        }));
    }

    private Mono<Boolean> whitelistAddress(String addressToWhitelist, String walletId, BcTransaction transaction, BigInteger role) {
        if(role.equals(BigInteger.ZERO)) return Mono.just(true);
        return bcSmartContractDaoService.findByType(SmartContractType.IDENTITY)
                .switchIfEmpty(Mono.error(new RuntimeException("IdentityManager not deployed")))
                .flatMap(id -> {
                    String contractAddress = id.getContractAddress();

                    return Mono.zip(blockChainService.getIdentityManager(adminCredentials, contractAddress), walletService.getOrCreateAdminWallet())
                            .flatMap(tuple -> {
                                IdentityManager identityManager = tuple.getT1();
                                BcWallet adminWallet = tuple.getT2();
                                return Mono.fromFuture(identityManager.hasRole(role, addressToWhitelist).sendAsync())
                                        .flatMap(res -> {
                                            if (res.component1()){
                                                log.info("Address {} already whitelisted with role {}", addressToWhitelist, role);
                                                return Mono.just(true);
                                            }

                                            String grantRoleFunctionCall = identityManager.grantRole(role, addressToWhitelist, BigInteger.ZERO).encodeFunctionCall();

                                            return web3Utils.prepareSafeTransaction(adminWallet.getWalletAddress(), identityManager.getContractAddress(), grantRoleFunctionCall)
                                                    .flatMap(call -> Mono.fromFuture(call.sendAsync())
                                                            .flatMap(transactionReceipt -> {
                                                                List<IdentityManager.RoleGrantedEventResponse> roleGrantedEvents = IdentityManager.getRoleGrantedEvents(transactionReceipt);
                                                                if(roleGrantedEvents.isEmpty()){
                                                                    log.error("Failed to whitelist address {}, no events found on tx", addressToWhitelist);
                                                                    return Mono.just(false);
                                                                }

                                                                IdentityManager.RoleGrantedEventResponse roleGrantedEvent = roleGrantedEvents.getFirst();

                                                                if (!roleGrantedEvent.roleId.equals(role) || !roleGrantedEvent.account.equalsIgnoreCase(addressToWhitelist)) {
                                                                    log.error("Failed to whitelist address {}, event data does not match, roleId={}, account={}, expectedRole={}",
                                                                            addressToWhitelist, roleGrantedEvent.roleId, roleGrantedEvent.account, role);
                                                                    return Mono.just(false);
                                                                }

                                                                log.info("Whitelisted address {} in tx {}", addressToWhitelist, transactionReceipt.getTransactionHash());
                                                                BcTransaction bcTransaction = TransactionUtils.setTransactionFields(transaction, transactionReceipt);

                                                                boolean isConfirmed = TransactionStatus.CONFIRMED.equals(bcTransaction.getStatus());

                                                                if (!isConfirmed) {
                                                                    log.error("Transaction status is not confirmed");
                                                                }

                                                                bcTransaction.setCreatorId(adminWallet.getId());
                                                                bcTransaction.setTransactionType(TransactionType.WHITELIST_WALLET);

                                                                if (walletId != null) {
                                                                    bcTransaction.setTransactionData("{\"walletId\":\"" + walletId + "\",\"role\":\"" + role + "\"}");
                                                                } else {
                                                                    bcTransaction.setTransactionData("{\"address\":\"" + addressToWhitelist + "\",\"role\":\"" + role + "\"}");
                                                                }

                                                                return bcTransactionDaoService.save(bcTransaction)
                                                                        .thenReturn(isConfirmed);
                                                            }));
                                        });
                            });
                })
                .onErrorResume((e) -> {
                    log.error("Error whitelisting wallet", e);
                    return Mono.just(false);
                });
    }
}
