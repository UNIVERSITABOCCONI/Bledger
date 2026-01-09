package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWalletServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private Credentials credentials;

    @Mock
    @SuppressWarnings("rawtypes")
    private Request request;

    @Mock
    private EthGetBalance response;

    @Test
    void getAdminBalanceUsesWeb3jBalance() {
        when(credentials.getAddress()).thenReturn("0xAdmin");
        when(response.getBalance()).thenReturn(BigInteger.valueOf(1_000_000_000_000_000_000L));
        when(request.sendAsync()).thenReturn(CompletableFuture.completedFuture(response));
        when(web3j.ethGetBalance(eq("0xAdmin"), eq(DefaultBlockParameterName.LATEST))).thenReturn(request);

        AdminWalletService service = new AdminWalletService(web3j, credentials);

        StepVerifier.create(service.getAdminBalance())
                .expectNextMatches(pair ->
                        "0xAdmin".equals(pair.getLeft())
                                && BigDecimal.ONE.compareTo(pair.getRight()) == 0
                )
                .verifyComplete();
    }
}
