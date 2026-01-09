package it.bocconi.bledger.blockchain.util.safe;

import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.2.
 */
@SuppressWarnings("rawtypes")
public class SafeProxyFactory extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_CREATECHAINSPECIFICPROXYWITHNONCE = "createChainSpecificProxyWithNonce";

    public static final String FUNC_CREATEPROXYWITHCALLBACK = "createProxyWithCallback";

    public static final String FUNC_CREATEPROXYWITHNONCE = "createProxyWithNonce";

    public static final String FUNC_GETCHAINID = "getChainId";

    public static final String FUNC_PROXYCREATIONCODE = "proxyCreationCode";

    public static final Event PROXYCREATION_EVENT = new Event("ProxyCreation", Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
    }, new TypeReference<Address>() {
    }));

    @Deprecated
    protected SafeProxyFactory(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SafeProxyFactory(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SafeProxyFactory(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SafeProxyFactory(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<ProxyCreationEventResponse> getProxyCreationEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PROXYCREATION_EVENT, transactionReceipt);
        ArrayList<ProxyCreationEventResponse> responses = new ArrayList<ProxyCreationEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ProxyCreationEventResponse typedResponse = new ProxyCreationEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.proxy = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.singleton = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ProxyCreationEventResponse getProxyCreationEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PROXYCREATION_EVENT, log);
        ProxyCreationEventResponse typedResponse = new ProxyCreationEventResponse();
        typedResponse.log = log;
        typedResponse.proxy = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.singleton = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ProxyCreationEventResponse> proxyCreationEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getProxyCreationEventFromLog(log));
    }

    public Flowable<ProxyCreationEventResponse> proxyCreationEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PROXYCREATION_EVENT));
        return proxyCreationEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> createChainSpecificProxyWithNonce(String singleton, byte[] initializer, BigInteger saltNonce) {
        final Function function = new Function(FUNC_CREATECHAINSPECIFICPROXYWITHNONCE, Arrays.<Type>asList(new Address(160, singleton),
                new DynamicBytes(initializer), new Uint256(saltNonce)), Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createProxyWithCallback(String singleton, byte[] initializer, BigInteger saltNonce, String callback) {
        final Function function = new Function(FUNC_CREATEPROXYWITHCALLBACK, Arrays.<Type>asList(new Address(160, singleton),
                new DynamicBytes(initializer), new Uint256(saltNonce), new Address(160, callback)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createProxyWithNonce(String singleton, byte[] initializer, BigInteger saltNonce) {
        final Function function = new Function(FUNC_CREATEPROXYWITHNONCE, Arrays.<Type>asList(new Address(160, singleton),
                new DynamicBytes(initializer), new Uint256(saltNonce)), Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getChainId() {
        final Function function = new Function(FUNC_GETCHAINID, Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
        }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<byte[]> proxyCreationCode() {
        final Function function = new Function(FUNC_PROXYCREATIONCODE, Arrays.<Type>asList(), Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {
        }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public byte[] staticProxyCreationCode() {
        String creationCode = "0x608060405234801561001057600080fd5b506040516101e63803806101e68339818101604052602081101561003357600080fd5b8101908080519060200190929190505050600073ffffffffffffffffff"
                + "ffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614156100ca576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401808060200182"
                + "8103825260228152602001806101c46022913960400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffff"
                + "ffffffff1602179055505060ab806101196000396000f3fe608060405273ffffffffffffffffffffffffffffffffffffffff600054167fa619486e0000000000000000000000000000000000000000000000000000000060"
                + "003514156050578060005260206000f35b3660008037600080366000845af43d6000803e60008114156070573d6000fd5b3d6000f3fea264697066735822122003d1488ee65e08fa41e58e888a9865554c535f2c77126a82"
                + "cb4c0f917f31441364736f6c63430007060033496e76616c69642073696e676c65746f6e20616464726573732070726f7669646564";
        return Numeric.hexStringToByteArray(creationCode);
    }

    @Deprecated
    public static SafeProxyFactory load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SafeProxyFactory(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SafeProxyFactory load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SafeProxyFactory(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SafeProxyFactory load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SafeProxyFactory(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SafeProxyFactory load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SafeProxyFactory(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class ProxyCreationEventResponse extends BaseEventResponse {
        public String proxy;

        public String singleton;
    }
}
