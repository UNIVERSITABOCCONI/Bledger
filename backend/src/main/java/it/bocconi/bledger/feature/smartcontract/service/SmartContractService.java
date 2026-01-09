package it.bocconi.bledger.feature.smartcontract.service;

import it.bocconi.bledger.blockchain.service.BlockChainService;
import it.bocconi.bledger.blockchain.smartcontract.generated.BLedgerNetwork;
import it.bocconi.bledger.blockchain.smartcontract.generated.IdentityManager;
import it.bocconi.bledger.exception.BadRequestException;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import it.bocconi.bledger.feature.transaction.dao.service.BcTransactionDaoService;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import it.bocconi.bledger.feature.transaction.util.TransactionUtils;
import it.bocconi.bledger.feature.wallet.entity.BcWallet;
import it.bocconi.bledger.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartContractService {

    private final BcSmartContractDaoService bcSmartContractDaoService;
    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final WalletService walletService;
    private final BlockChainService blockChainService;
    private final BcTransactionDaoService bcTransactionDaoService;
    private static final String NFT_NAME   = "BLedger Network";
    private static final String NFT_SYMBOL = "BLN";


    @Transactional
    public Mono<BcSmartContract> deploySmartContract(SmartContractType scType) {
        return deploySmartContractByType(scType)
                .flatMap(address -> {
                    BcSmartContract smartContract = BcSmartContract.builder()
                            .contractAddress(address)
                            .type(scType)
                            .build();

                    return bcSmartContractDaoService.save(smartContract);
                });
    }

    private Mono<String> deploySmartContractByType(SmartContractType scType) {
        return walletService.getOrCreateAdminWallet()
                .flatMap(adminWallet -> bcSmartContractDaoService.existByType(SmartContractType.IDENTITY)
                        .flatMap(exists -> {
                            if(scType == SmartContractType.IDENTITY){
                                if(exists) return Mono.error(new BadRequestException("Identity Manager already deployed"));
                                return deployIdentityManager(adminWallet);
                            } else if (scType == SmartContractType.ERC721) {
                                if (!exists) return Mono.error(new BadRequestException("Identity Manager not deployed"));
                                return deployERC721(adminWallet);
                            } else return Mono.error(new BadRequestException("Unsopported contract type"));
                        }));
    }

    private Mono<String> deployERC721(BcWallet adminWallet) {

        Mono<ContractGasProvider> gasProviderMono = blockChainService.getGasProvider();
        Mono<BcSmartContract> identityManagerMono = bcSmartContractDaoService.findByType(SmartContractType.IDENTITY);
        return Mono.zip(gasProviderMono, identityManagerMono)
                .flatMap(tuple -> Mono.fromFuture(BLedgerNetwork.deploy(web3j, blockChainService.getTransactionManager(adminCredentials, web3j), tuple.getT1(), tuple.getT2().getContractAddress(), NFT_NAME, NFT_SYMBOL).sendAsync()))
                .flatMap(deployedIdm -> {
                    String contractAddress = deployedIdm.getContractAddress();
                    TransactionReceipt transactionReceipt = deployedIdm.getTransactionReceipt().orElse(null);
                    return saveDeployTransaction(contractAddress, transactionReceipt, adminWallet.getId(), SmartContractType.ERC721)
                            .thenReturn(contractAddress);
                });
    }

    private Mono<String> deployIdentityManager(BcWallet adminWallet) {
        return blockChainService.getGasProvider()
                .flatMap(contractGasProvider -> Mono.fromFuture(IdentityManager.deploy(web3j, blockChainService.getTransactionManager(adminCredentials, web3j), contractGasProvider, adminWallet.getWalletAddress()).sendAsync()))
                .flatMap(deployedIdm -> {
                    String contractAddress = deployedIdm.getContractAddress();
                    TransactionReceipt transactionReceipt = deployedIdm.getTransactionReceipt().orElse(null);
                    return saveDeployTransaction(contractAddress, transactionReceipt, adminWallet.getId(), SmartContractType.IDENTITY)
                            .thenReturn(contractAddress);
                });
    }

    private Mono<BcTransaction> saveDeployTransaction(String contractAddress, TransactionReceipt transactionReceipt, String adminWalletId, SmartContractType scType) {
        BcTransaction tx = TransactionUtils.setTransactionFields(new BcTransaction(), transactionReceipt);
        tx.setTransactionType(TransactionType.DEPLOY_SMART_CONTRACT);
        tx.setCreatorId(adminWalletId);
        tx.setTransactionData("{\"contractAddress\":\"" + contractAddress + "\",\"type\":\"" + scType + "\"}");
        return bcTransactionDaoService.save(tx);
    }

}
