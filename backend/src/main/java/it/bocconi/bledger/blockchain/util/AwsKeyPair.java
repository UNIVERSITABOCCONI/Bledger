package it.bocconi.bledger.blockchain.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.math.BigInteger;
import java.util.Arrays;


@Slf4j
public class AwsKeyPair extends ECKeyPair {

    private static final KmsClient CLIENT = KmsClient.create();
    private final String keyId;
    private final BigInteger publicKey;

    public AwsKeyPair(String keyId) {
        super(null, null);
        this.keyId = keyId;
        log.info("Loading AWSECKeyPair: keyId={}", keyId);
        byte[] derPublicKey = CLIENT
                .getPublicKey((var builder) -> builder.keyId(keyId))
                .publicKey()
                .asByteArray();
        byte[] publicKeyBytes = SubjectPublicKeyInfo
                .getInstance(derPublicKey)
                .getPublicKeyData()
                .getBytes();
        this.publicKey = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        log.info("AWSECKeyPair loaded: publicKey={}, address={}", this.publicKey, Numeric.prependHexPrefix(Keys.getAddress(this)));
    }

    @Override
    public BigInteger getPrivateKey() {
        throw new UnsupportedOperationException("Private key is not accessible when using AWS KMS");
    }

    @Override
    public BigInteger getPublicKey() {
        return publicKey;
    }

    @Override
    public ECDSASignature sign(byte[] transactionHash) {
        SignResponse sign = CLIENT.sign((var builder) -> builder.keyId(keyId)
                .messageType(MessageType.DIGEST)
                .message(SdkBytes.fromByteArray(transactionHash))
                .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256));
        ASN1Sequence instance = ASN1Sequence.getInstance(sign.signature().asByteArray());
        ASN1Integer r = (ASN1Integer) instance.getObjectAt(0);
        ASN1Integer s = (ASN1Integer) instance.getObjectAt(1);
        return new ECDSASignature(r.getValue(), s.getValue()).toCanonicalised();
    }
}
