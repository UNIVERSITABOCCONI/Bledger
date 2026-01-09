package it.bocconi.bledger.service;

import it.bocconi.bledger.blockchain.config.ChainIdWrapper;
import it.bocconi.bledger.blockchain.config.Web3jConfig;
import it.bocconi.bledger.blockchain.service.BlockChainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockChainServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private Web3jConfig web3jConfig;

    @Mock
    private Credentials credentials;

    @Mock
    private ChainIdWrapper chainIdWrapper;

    @Mock
    @SuppressWarnings("rawtypes")
    private Request priorityRequest;

    @Mock
    @SuppressWarnings("rawtypes")
    private Request blockRequest;

    @Test
    void getGasProviderUsesEip1559Values() {
        EthMaxPriorityFeePerGas priorityResponse = mock(EthMaxPriorityFeePerGas.class);
        EthBlock blockResponse = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        when(web3jConfig.getGasLimit()).thenReturn(BigInteger.valueOf(100_000L));
        when(chainIdWrapper.getChainId()).thenReturn(1L);
        when(web3j.ethMaxPriorityFeePerGas()).thenReturn(priorityRequest);
        when(web3j.ethGetBlockByNumber(eq(DefaultBlockParameterName.LATEST), eq(false))).thenReturn(blockRequest);
        when(priorityRequest.sendAsync()).thenReturn(CompletableFuture.completedFuture(priorityResponse));
        when(blockRequest.sendAsync()).thenReturn(CompletableFuture.completedFuture(blockResponse));
        when(priorityResponse.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(1_000_000_000L));
        when(blockResponse.getBlock()).thenReturn(block);
        when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(2_000_000_000L));

        BlockChainService service = new BlockChainService(web3jConfig, web3j, credentials, chainIdWrapper);

        StepVerifier.create(service.getGasProvider())
                .expectNextMatches(provider ->
                        BigInteger.valueOf(100_000L).equals(provider.getGasLimit())
                )
                .verifyComplete();
    }
}
