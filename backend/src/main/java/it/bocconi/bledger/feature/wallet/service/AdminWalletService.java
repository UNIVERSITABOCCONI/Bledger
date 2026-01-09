package it.bocconi.bledger.feature.wallet.service;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class AdminWalletService {

    private Web3j web3j;
    private Credentials adminCredentials;

    public Mono<Pair<String, BigDecimal>> getAdminBalance() {
        return Mono.fromFuture(web3j.ethGetBalance(adminCredentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync())
                .map(result -> Pair.of(adminCredentials.getAddress(), Convert.fromWei(result.getBalance().toString(), Convert.Unit.ETHER)));
    }

    @PostConstruct
    public void checkAdminBalance() {
        getAdminBalance()
                .doOnSuccess(result -> log.info("Admin balance {} : {} ", result.getLeft(), result.getRight())).subscribe();
    }
}
