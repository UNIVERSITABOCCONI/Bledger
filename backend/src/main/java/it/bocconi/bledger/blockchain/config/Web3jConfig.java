package it.bocconi.bledger.blockchain.config;


import it.bocconi.bledger.blockchain.util.AwsKeyPair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Slf4j
public class Web3jConfig {

    @Getter
    @Value("${it.bocconi.bledger.blockchain.config.adminWalletKmsKeyId:}")
    private String walletKmsKeyArn;

    @Value("${it.bocconi.bledger.blockchain.config.adminWalletPrivateKey:}")
    @Getter
    private String walletPrivateKey;

    @Value("${it.bocconi.bledger.blockchain.config.rpcUrl:''}")
    @Getter
    private String rpcUrl;

    @Value("${it.bocconi.bledger.blockchain.config.defaultBlockTime:6}")
    @Getter
    private long defaultBlockTime;

    @Value("${it.bocconi.bledger.blockchain.config.proxySafeFactoryAddress:''}")
    @Getter
    private String proxySafeFactoryAddress;

    @Getter
    @Value("${it.bocconi.bledger.blockchain.config.safeFallbackAddress:''}")
    private String safeFallbackAddress;

    @Getter
    @Value("${it.bocconi.bledger.blockchain.config.gnosisSafeL2:''}")
    private String gnosisSafeL2;

    @Getter
    @Value("${it.bocconi.bledger.blockchain.config.useKmsKey:true}")
    private Boolean useKmsKey;

    @Value("${it.bocconi.bledger.blockchain.coupon.gas-units-per-mint:85000}")
    @Getter
    private long gasUnitsPerMint;

    @Value("${it.bocconi.bledger.blockchain.coupon.fixed-gas-per-transaction:250000}")
    @Getter
    private long fixedGasPerTransaction;

    @Value("${it.bocconi.bledger.blockchain.polygon.defaultGasPrice:50000000000}")
    private long polygonDefaultGasPrice;

    @Value("${it.bocconi.bledger.blockchain.polygon.gasLimit:5000000}")
    private long gasLimit;


    public BigInteger getPolygonDefaultGasPrice() {
        return BigInteger.valueOf(polygonDefaultGasPrice);
    }

    public BigInteger getGasLimit() {
        return BigInteger.valueOf(gasLimit);
    }


    @Bean
    public Credentials getAdminCredentials() {
        Credentials credentials = null;
        if (useKmsKey && walletKmsKeyArn != null && !walletKmsKeyArn.isEmpty()) {
            credentials = Credentials.create(new AwsKeyPair(walletKmsKeyArn));
        } else if (!useKmsKey && walletPrivateKey != null && !walletPrivateKey.isEmpty()) {
            // Private key should be used only for test / local environment
            credentials = Credentials.create(walletPrivateKey);
        }
        log.info("Admin Credentials address in use: {}", credentials.getAddress());
        return credentials;
    }

    @Bean
    public ChainIdWrapper chainId(Web3j web3j) {
            try {
                return new ChainIdWrapper(web3j.ethChainId().send().getChainId().longValue());
            } catch (IOException e) {
                log.error("Error getting chainId", e);
                throw new RuntimeException("Error getting chainId");
            }
    }


    @Bean
    public Web3j web3j(
            ScheduledExecutorService web3jScheduler) {
        return Web3j.build(
                new HttpService(rpcUrl),
                defaultBlockTime * 1000L,
                web3jScheduler);
    }


    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService web3jScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r ->
                new Thread(r, "web3j-scheduler"));
    }




}
