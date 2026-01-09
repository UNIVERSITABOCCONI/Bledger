package it.bocconi.bledger.blockchain.service;


import it.bocconi.bledger.blockchain.config.ChainIdWrapper;
import it.bocconi.bledger.blockchain.config.Web3jConfig;
import it.bocconi.bledger.blockchain.smartcontract.generated.BLedgerNetwork;
import it.bocconi.bledger.blockchain.smartcontract.generated.IdentityManager;
import it.bocconi.bledger.blockchain.util.safe.SafeL2;
import it.bocconi.bledger.blockchain.util.safe.SafeProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.utils.Convert;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockChainService {

    private final Web3jConfig web3jConfig;
    private final Web3j web3j;
    private final Credentials adminCredentials;
    private final ChainIdWrapper chainIdWrapper;


    private Mono<BigInteger> getGasPrice() {
        return Mono.fromFuture(web3j.ethGasPrice().sendAsync())
                .flatMap(gasPriceWrapper -> {
                    BigInteger gasPrice = gasPriceWrapper.getGasPrice();
                    log.info("Gas price from node: {}", Convert.fromWei(gasPrice.toString(), Convert.Unit.GWEI));

                    if (gasPrice.compareTo(BigInteger.valueOf(1000000000L)) < 0) {
                        log.info("Gas price is less than 1 gwei, setting it to 1 gwei");
                        gasPrice = BigInteger.valueOf(1000000000L);
                    }
                    BigInteger increasedGas = gasPrice.multiply(BigInteger.valueOf(12)).divide(BigInteger.valueOf(10));
                    log.info("Increased gas price: {}", Convert.fromWei(increasedGas.toString(), Convert.Unit.GWEI));
                    return Mono.just(increasedGas);
                });
    }

    public RawTransactionManager getTransactionManager(Credentials credentials, Web3j web3) {
        return getTransactionManager(credentials, web3, false);
    }

    public RawTransactionManager getTransactionManager(Credentials credentials, Web3j web3, Boolean noOp) {
        if (noOp) {
            return new RawTransactionManager(web3, credentials, chainIdWrapper.getChainId(), new NoOpProcessor(web3));
        }
        return new RawTransactionManager(web3, credentials, chainIdWrapper.getChainId(), 300, web3jConfig.getDefaultBlockTime() * 1000);
    }

    public SafeL2 getReadOnlySafeL2() {
        return SafeL2.load(web3jConfig.getProxySafeFactoryAddress(), web3j, new ReadonlyTransactionManager(web3j, Address.DEFAULT.toString()), new StaticGasProvider(BigInteger.ZERO, DefaultGasProvider.GAS_LIMIT));
    }

    public Mono<SafeL2> getSafeL2(Credentials credentials, String address, boolean async) {
        return getSafeL2(credentials, address, async, null);
    }

    public Mono<SafeL2> getSafeL2(Credentials credentials, String address, boolean async, Integer batchCount) {
        BigInteger gasLimitToUse =
                (batchCount != null)
                        ? BigInteger.valueOf(web3jConfig.getFixedGasPerTransaction()
                        + (long) batchCount * web3jConfig.getGasUnitsPerMint())
                        : web3jConfig.getGasLimit();

        return getGasProvider(gasLimitToUse)
                .map(gp -> SafeL2.load(address, web3j, getTransactionManager(credentials, web3j, async), gp));
    }

    public SafeProxyFactory getReadOnlySafeProxyFactory() {
        return SafeProxyFactory.load(web3jConfig.getProxySafeFactoryAddress(), web3j, adminCredentials, new StaticGasProvider(BigInteger.ZERO, DefaultGasProvider.GAS_LIMIT));
    }

    public Mono<SafeProxyFactory> getSafeProxyFactory(Credentials credentials) {
        return getGasProvider()
                .map(gp -> SafeProxyFactory.load(
                        web3jConfig.getProxySafeFactoryAddress(), web3j, getTransactionManager(credentials, web3j), gp
                ));
    }

    public Mono<IdentityManager> getIdentityManager(Credentials credentials, String smartContractAddress) {
        return getGasProvider()
                .map(gp -> IdentityManager.load(smartContractAddress, web3j, getTransactionManager(credentials, web3j), gp));
    }

    public Mono<ContractGasProvider> getGasProvider() {
        return getGasProvider(web3jConfig.getGasLimit());
    }

    public Mono<ContractGasProvider> getGasProvider(BigInteger gasLimit) {
            Mono<BigInteger> maxPriorityFeePerGasMono = Mono.fromFuture(web3j.ethMaxPriorityFeePerGas().sendAsync())
                    .map(EthMaxPriorityFeePerGas::getMaxPriorityFeePerGas);

            Mono<BigInteger> baseFeePerGasMono = Mono.fromFuture(web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).sendAsync())
                    .map(result -> result.getBlock().getBaseFeePerGas());

        return Mono.zip(maxPriorityFeePerGasMono, baseFeePerGasMono)
                .map(tuple -> {
                    BigInteger tip = tuple.getT1();
                    BigInteger base = tuple.getT2();
                    BigInteger maxFee = base.multiply(BigInteger.valueOf(2)).add(tip);
                    log.debug("maxPriorityFeePerGas: {}, baseFeePerGas: {}, maxFeePerGas: {}", tip, base, maxFee);
                    return (ContractGasProvider) new StaticEIP1559GasProvider(
                            chainIdWrapper.getChainId(), maxFee, tip, gasLimit
                    );
                })
                .onErrorResume(ex ->
                        getGasPrice().map(gp -> new StaticGasProvider(gp, gasLimit))
                );

    }

    public Mono<BLedgerNetwork> bLedgerNetwork(Credentials credentials, String smartContractAddress){
        return getGasProvider()
                .map(gas -> BLedgerNetwork.load(smartContractAddress, web3j, getTransactionManager(credentials, web3j, false), gas)
                        );
    }

}
