package it.bocconi.bledger.blockchain.util;

import it.bocconi.bledger.blockchain.config.Web3jConfig;
import it.bocconi.bledger.blockchain.service.BlockChainService;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class Web3Utils {

    private final Credentials adminCredentials;
    private final BlockChainService blockChainService;
    public static BigInteger generateNonce() {
        return new BigInteger(30, new SecureRandom());
    }

    public Mono<RemoteFunctionCall<TransactionReceipt>> prepareSafeTransaction(String safeAddress, String toAddress, String encodedFunction) {
        return prepareSafeTransaction(safeAddress, toAddress, encodedFunction, false);
    }

    public Mono<RemoteFunctionCall<TransactionReceipt>> prepareSafeTransaction(String safeAddress, String toAddress, String encodedFunction, boolean async) {
        return prepareSafeTransaction(safeAddress, toAddress, encodedFunction, async, null);
    }

    public Mono<RemoteFunctionCall<TransactionReceipt>> prepareSafeTransaction(String safeAddress, String toAddress, String encodedFunction, boolean async, Integer batchCount) {
        return blockChainService.getSafeL2(adminCredentials, safeAddress, async, batchCount)
                .flatMap(safeL2 -> {
                    String zeroAddress = Address.DEFAULT.toString();
                    return Mono.fromFuture(safeL2.nonce().sendAsync())
                            .flatMap(nonce -> Mono.fromFuture(safeL2.getTransactionHash(toAddress, BigInteger.ZERO, Numeric.hexStringToByteArray(encodedFunction), BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                                    zeroAddress, zeroAddress, nonce).sendAsync())
                                    .map(hash -> {
                                        byte[] signatureBytes = new byte[65];

                                        Sign.SignatureData signature = Sign.signMessage(hash, adminCredentials.getEcKeyPair(), false);
                                        System.arraycopy(signature.getR(), 0, signatureBytes, 0, 32);
                                        System.arraycopy(signature.getS(), 0, signatureBytes, 32, 32);
                                        System.arraycopy(signature.getV(), 0, signatureBytes, 64, 1);

                                        return safeL2.execTransaction(toAddress, BigInteger.ZERO, Numeric.hexStringToByteArray(encodedFunction),
                                                BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, zeroAddress, zeroAddress,
                                                signatureBytes, BigInteger.ZERO);
                                    }));

                });


    }

    private static byte[] keccak256(String input) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }
}
