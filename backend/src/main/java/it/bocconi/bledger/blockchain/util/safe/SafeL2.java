package it.bocconi.bledger.blockchain.util.safe;

import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

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
public class SafeL2 extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_VERSION = "VERSION";

    public static final String FUNC_ADDOWNERWITHTHRESHOLD = "addOwnerWithThreshold";

    public static final String FUNC_APPROVEHASH = "approveHash";

    public static final String FUNC_APPROVEDHASHES = "approvedHashes";

    public static final String FUNC_CHANGETHRESHOLD = "changeThreshold";

    public static final String FUNC_CHECKNSIGNATURES = "checkNSignatures";

    public static final String FUNC_CHECKSIGNATURES = "checkSignatures";

    public static final String FUNC_DISABLEMODULE = "disableModule";

    public static final String FUNC_DOMAINSEPARATOR = "domainSeparator";

    public static final String FUNC_ENABLEMODULE = "enableModule";

    public static final String FUNC_ENCODETRANSACTIONDATA = "encodeTransactionData";

    public static final String FUNC_EXECTRANSACTION = "execTransaction";

    public static final String FUNC_EXECTRANSACTIONFROMMODULE = "execTransactionFromModule";

    public static final String FUNC_EXECTRANSACTIONFROMMODULERETURNDATA = "execTransactionFromModuleReturnData";

    public static final String FUNC_GETCHAINID = "getChainId";

    public static final String FUNC_GETMODULESPAGINATED = "getModulesPaginated";

    public static final String FUNC_GETOWNERS = "getOwners";

    public static final String FUNC_GETSTORAGEAT = "getStorageAt";

    public static final String FUNC_GETTHRESHOLD = "getThreshold";

    public static final String FUNC_GETTRANSACTIONHASH = "getTransactionHash";

    public static final String FUNC_ISMODULEENABLED = "isModuleEnabled";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_NONCE = "nonce";

    public static final String FUNC_REMOVEOWNER = "removeOwner";

    public static final String FUNC_SETFALLBACKHANDLER = "setFallbackHandler";

    public static final String FUNC_SETGUARD = "setGuard";

    public static final String FUNC_SETUP = "setup";

    public static final String FUNC_SIGNEDMESSAGES = "signedMessages";

    public static final String FUNC_SIMULATEANDREVERT = "simulateAndRevert";

    public static final String FUNC_SWAPOWNER = "swapOwner";

    public static final Event ADDEDOWNER_EVENT = new Event("AddedOwner",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event APPROVEHASH_EVENT = new Event("ApproveHash",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Address>(true) {
            }));

    public static final Event CHANGEDFALLBACKHANDLER_EVENT = new Event("ChangedFallbackHandler",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event CHANGEDGUARD_EVENT = new Event("ChangedGuard",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event CHANGEDTHRESHOLD_EVENT = new Event("ChangedThreshold",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));

    public static final Event DISABLEDMODULE_EVENT = new Event("DisabledModule",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event ENABLEDMODULE_EVENT = new Event("EnabledModule",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event EXECUTIONFAILURE_EVENT = new Event("ExecutionFailure",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Uint256>() {
            }));

    public static final Event EXECUTIONFROMMODULEFAILURE_EVENT = new Event("ExecutionFromModuleFailure",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event EXECUTIONFROMMODULESUCCESS_EVENT = new Event("ExecutionFromModuleSuccess",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event EXECUTIONSUCCESS_EVENT = new Event("ExecutionSuccess",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }, new TypeReference<Uint256>() {
            }));

    public static final Event REMOVEDOWNER_EVENT = new Event("RemovedOwner",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }));

    public static final Event SAFEMODULETRANSACTION_EVENT = new Event("SafeModuleTransaction",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<DynamicBytes>() {
            }, new TypeReference<Uint8>() {
            }));

    public static final Event SAFEMULTISIGTRANSACTION_EVENT = new Event("SafeMultiSigTransaction",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<DynamicBytes>() {
            }, new TypeReference<Uint8>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<DynamicBytes>() {
            }, new TypeReference<DynamicBytes>() {
            }));

    public static final Event SAFERECEIVED_EVENT = new Event("SafeReceived",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }, new TypeReference<Uint256>() {
            }));

    public static final Event SAFESETUP_EVENT = new Event("SafeSetup",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
            }, new TypeReference<DynamicArray<Address>>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }));

    public static final Event SIGNMSG_EVENT = new Event("SignMsg",
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {
            }));

    @Deprecated
    protected SafeL2(String contractAddress, Web3j web3j, Credentials credentials,
                     BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SafeL2(String contractAddress, Web3j web3j, Credentials credentials,
                     ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SafeL2(String contractAddress, Web3j web3j,
                     TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SafeL2(String contractAddress, Web3j web3j,
                     TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<AddedOwnerEventResponse> getAddedOwnerEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ADDEDOWNER_EVENT, transactionReceipt);
        ArrayList<AddedOwnerEventResponse> responses = new ArrayList<AddedOwnerEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            AddedOwnerEventResponse typedResponse = new AddedOwnerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AddedOwnerEventResponse getAddedOwnerEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ADDEDOWNER_EVENT, log);
        AddedOwnerEventResponse typedResponse = new AddedOwnerEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<AddedOwnerEventResponse> addedOwnerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAddedOwnerEventFromLog(log));
    }

    public Flowable<AddedOwnerEventResponse> addedOwnerEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ADDEDOWNER_EVENT));
        return addedOwnerEventFlowable(filter);
    }

    public static List<ApproveHashEventResponse> getApproveHashEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVEHASH_EVENT, transactionReceipt);
        ArrayList<ApproveHashEventResponse> responses = new ArrayList<ApproveHashEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ApproveHashEventResponse typedResponse = new ApproveHashEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.approvedHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApproveHashEventResponse getApproveHashEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVEHASH_EVENT, log);
        ApproveHashEventResponse typedResponse = new ApproveHashEventResponse();
        typedResponse.log = log;
        typedResponse.approvedHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<ApproveHashEventResponse> approveHashEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApproveHashEventFromLog(log));
    }

    public Flowable<ApproveHashEventResponse> approveHashEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVEHASH_EVENT));
        return approveHashEventFlowable(filter);
    }

    public static List<ChangedFallbackHandlerEventResponse> getChangedFallbackHandlerEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CHANGEDFALLBACKHANDLER_EVENT, transactionReceipt);
        ArrayList<ChangedFallbackHandlerEventResponse> responses = new ArrayList<ChangedFallbackHandlerEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ChangedFallbackHandlerEventResponse typedResponse = new ChangedFallbackHandlerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.handler = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ChangedFallbackHandlerEventResponse getChangedFallbackHandlerEventFromLog(
            Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CHANGEDFALLBACKHANDLER_EVENT, log);
        ChangedFallbackHandlerEventResponse typedResponse = new ChangedFallbackHandlerEventResponse();
        typedResponse.log = log;
        typedResponse.handler = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ChangedFallbackHandlerEventResponse> changedFallbackHandlerEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getChangedFallbackHandlerEventFromLog(log));
    }

    public Flowable<ChangedFallbackHandlerEventResponse> changedFallbackHandlerEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANGEDFALLBACKHANDLER_EVENT));
        return changedFallbackHandlerEventFlowable(filter);
    }

    public static List<ChangedGuardEventResponse> getChangedGuardEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CHANGEDGUARD_EVENT, transactionReceipt);
        ArrayList<ChangedGuardEventResponse> responses = new ArrayList<ChangedGuardEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ChangedGuardEventResponse typedResponse = new ChangedGuardEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.guard = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ChangedGuardEventResponse getChangedGuardEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CHANGEDGUARD_EVENT, log);
        ChangedGuardEventResponse typedResponse = new ChangedGuardEventResponse();
        typedResponse.log = log;
        typedResponse.guard = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ChangedGuardEventResponse> changedGuardEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getChangedGuardEventFromLog(log));
    }

    public Flowable<ChangedGuardEventResponse> changedGuardEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANGEDGUARD_EVENT));
        return changedGuardEventFlowable(filter);
    }

    public static List<ChangedThresholdEventResponse> getChangedThresholdEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CHANGEDTHRESHOLD_EVENT, transactionReceipt);
        ArrayList<ChangedThresholdEventResponse> responses = new ArrayList<ChangedThresholdEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ChangedThresholdEventResponse typedResponse = new ChangedThresholdEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.threshold = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ChangedThresholdEventResponse getChangedThresholdEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CHANGEDTHRESHOLD_EVENT, log);
        ChangedThresholdEventResponse typedResponse = new ChangedThresholdEventResponse();
        typedResponse.log = log;
        typedResponse.threshold = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ChangedThresholdEventResponse> changedThresholdEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getChangedThresholdEventFromLog(log));
    }

    public Flowable<ChangedThresholdEventResponse> changedThresholdEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANGEDTHRESHOLD_EVENT));
        return changedThresholdEventFlowable(filter);
    }

    public static List<DisabledModuleEventResponse> getDisabledModuleEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DISABLEDMODULE_EVENT, transactionReceipt);
        ArrayList<DisabledModuleEventResponse> responses = new ArrayList<DisabledModuleEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            DisabledModuleEventResponse typedResponse = new DisabledModuleEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DisabledModuleEventResponse getDisabledModuleEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DISABLEDMODULE_EVENT, log);
        DisabledModuleEventResponse typedResponse = new DisabledModuleEventResponse();
        typedResponse.log = log;
        typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DisabledModuleEventResponse> disabledModuleEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDisabledModuleEventFromLog(log));
    }

    public Flowable<DisabledModuleEventResponse> disabledModuleEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DISABLEDMODULE_EVENT));
        return disabledModuleEventFlowable(filter);
    }

    public static List<EnabledModuleEventResponse> getEnabledModuleEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ENABLEDMODULE_EVENT, transactionReceipt);
        ArrayList<EnabledModuleEventResponse> responses = new ArrayList<EnabledModuleEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            EnabledModuleEventResponse typedResponse = new EnabledModuleEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static EnabledModuleEventResponse getEnabledModuleEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ENABLEDMODULE_EVENT, log);
        EnabledModuleEventResponse typedResponse = new EnabledModuleEventResponse();
        typedResponse.log = log;
        typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<EnabledModuleEventResponse> enabledModuleEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getEnabledModuleEventFromLog(log));
    }

    public Flowable<EnabledModuleEventResponse> enabledModuleEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ENABLEDMODULE_EVENT));
        return enabledModuleEventFlowable(filter);
    }

    public static List<ExecutionFailureEventResponse> getExecutionFailureEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EXECUTIONFAILURE_EVENT, transactionReceipt);
        ArrayList<ExecutionFailureEventResponse> responses = new ArrayList<ExecutionFailureEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ExecutionFailureEventResponse typedResponse = new ExecutionFailureEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.txHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.payment = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ExecutionFailureEventResponse getExecutionFailureEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EXECUTIONFAILURE_EVENT, log);
        ExecutionFailureEventResponse typedResponse = new ExecutionFailureEventResponse();
        typedResponse.log = log;
        typedResponse.txHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.payment = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ExecutionFailureEventResponse> executionFailureEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getExecutionFailureEventFromLog(log));
    }

    public Flowable<ExecutionFailureEventResponse> executionFailureEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXECUTIONFAILURE_EVENT));
        return executionFailureEventFlowable(filter);
    }

    public static List<ExecutionFromModuleFailureEventResponse> getExecutionFromModuleFailureEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EXECUTIONFROMMODULEFAILURE_EVENT, transactionReceipt);
        ArrayList<ExecutionFromModuleFailureEventResponse> responses = new ArrayList<ExecutionFromModuleFailureEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ExecutionFromModuleFailureEventResponse typedResponse = new ExecutionFromModuleFailureEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ExecutionFromModuleFailureEventResponse getExecutionFromModuleFailureEventFromLog(
            Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EXECUTIONFROMMODULEFAILURE_EVENT, log);
        ExecutionFromModuleFailureEventResponse typedResponse = new ExecutionFromModuleFailureEventResponse();
        typedResponse.log = log;
        typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ExecutionFromModuleFailureEventResponse> executionFromModuleFailureEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getExecutionFromModuleFailureEventFromLog(log));
    }

    public Flowable<ExecutionFromModuleFailureEventResponse> executionFromModuleFailureEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXECUTIONFROMMODULEFAILURE_EVENT));
        return executionFromModuleFailureEventFlowable(filter);
    }

    public static List<ExecutionFromModuleSuccessEventResponse> getExecutionFromModuleSuccessEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EXECUTIONFROMMODULESUCCESS_EVENT, transactionReceipt);
        ArrayList<ExecutionFromModuleSuccessEventResponse> responses = new ArrayList<ExecutionFromModuleSuccessEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ExecutionFromModuleSuccessEventResponse typedResponse = new ExecutionFromModuleSuccessEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ExecutionFromModuleSuccessEventResponse getExecutionFromModuleSuccessEventFromLog(
            Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EXECUTIONFROMMODULESUCCESS_EVENT, log);
        ExecutionFromModuleSuccessEventResponse typedResponse = new ExecutionFromModuleSuccessEventResponse();
        typedResponse.log = log;
        typedResponse.module = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ExecutionFromModuleSuccessEventResponse> executionFromModuleSuccessEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getExecutionFromModuleSuccessEventFromLog(log));
    }

    public Flowable<ExecutionFromModuleSuccessEventResponse> executionFromModuleSuccessEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXECUTIONFROMMODULESUCCESS_EVENT));
        return executionFromModuleSuccessEventFlowable(filter);
    }

    public static List<ExecutionSuccessEventResponse> getExecutionSuccessEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(EXECUTIONSUCCESS_EVENT, transactionReceipt);
        ArrayList<ExecutionSuccessEventResponse> responses = new ArrayList<ExecutionSuccessEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ExecutionSuccessEventResponse typedResponse = new ExecutionSuccessEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.txHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.payment = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ExecutionSuccessEventResponse getExecutionSuccessEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(EXECUTIONSUCCESS_EVENT, log);
        ExecutionSuccessEventResponse typedResponse = new ExecutionSuccessEventResponse();
        typedResponse.log = log;
        typedResponse.txHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.payment = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ExecutionSuccessEventResponse> executionSuccessEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getExecutionSuccessEventFromLog(log));
    }

    public Flowable<ExecutionSuccessEventResponse> executionSuccessEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(EXECUTIONSUCCESS_EVENT));
        return executionSuccessEventFlowable(filter);
    }

    public static List<RemovedOwnerEventResponse> getRemovedOwnerEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REMOVEDOWNER_EVENT, transactionReceipt);
        ArrayList<RemovedOwnerEventResponse> responses = new ArrayList<RemovedOwnerEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RemovedOwnerEventResponse typedResponse = new RemovedOwnerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RemovedOwnerEventResponse getRemovedOwnerEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REMOVEDOWNER_EVENT, log);
        RemovedOwnerEventResponse typedResponse = new RemovedOwnerEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<RemovedOwnerEventResponse> removedOwnerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRemovedOwnerEventFromLog(log));
    }

    public Flowable<RemovedOwnerEventResponse> removedOwnerEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REMOVEDOWNER_EVENT));
        return removedOwnerEventFlowable(filter);
    }

    public static List<SafeModuleTransactionEventResponse> getSafeModuleTransactionEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SAFEMODULETRANSACTION_EVENT, transactionReceipt);
        ArrayList<SafeModuleTransactionEventResponse> responses = new ArrayList<SafeModuleTransactionEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SafeModuleTransactionEventResponse typedResponse = new SafeModuleTransactionEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.module = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.operation = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SafeModuleTransactionEventResponse getSafeModuleTransactionEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SAFEMODULETRANSACTION_EVENT, log);
        SafeModuleTransactionEventResponse typedResponse = new SafeModuleTransactionEventResponse();
        typedResponse.log = log;
        typedResponse.module = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.operation = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        return typedResponse;
    }

    public Flowable<SafeModuleTransactionEventResponse> safeModuleTransactionEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSafeModuleTransactionEventFromLog(log));
    }

    public Flowable<SafeModuleTransactionEventResponse> safeModuleTransactionEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SAFEMODULETRANSACTION_EVENT));
        return safeModuleTransactionEventFlowable(filter);
    }

    public static List<SafeMultiSigTransactionEventResponse> getSafeMultiSigTransactionEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SAFEMULTISIGTRANSACTION_EVENT, transactionReceipt);
        ArrayList<SafeMultiSigTransactionEventResponse> responses = new ArrayList<SafeMultiSigTransactionEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SafeMultiSigTransactionEventResponse typedResponse = new SafeMultiSigTransactionEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.operation = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.safeTxGas = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.baseGas = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.gasPrice = (BigInteger) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.gasToken = (String) eventValues.getNonIndexedValues().get(7).getValue();
            typedResponse.refundReceiver = (String) eventValues.getNonIndexedValues().get(8).getValue();
            typedResponse.signatures = (byte[]) eventValues.getNonIndexedValues().get(9).getValue();
            typedResponse.additionalInfo = (byte[]) eventValues.getNonIndexedValues().get(10).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SafeMultiSigTransactionEventResponse getSafeMultiSigTransactionEventFromLog(
            Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SAFEMULTISIGTRANSACTION_EVENT, log);
        SafeMultiSigTransactionEventResponse typedResponse = new SafeMultiSigTransactionEventResponse();
        typedResponse.log = log;
        typedResponse.to = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.operation = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.safeTxGas = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.baseGas = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.gasPrice = (BigInteger) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.gasToken = (String) eventValues.getNonIndexedValues().get(7).getValue();
        typedResponse.refundReceiver = (String) eventValues.getNonIndexedValues().get(8).getValue();
        typedResponse.signatures = (byte[]) eventValues.getNonIndexedValues().get(9).getValue();
        typedResponse.additionalInfo = (byte[]) eventValues.getNonIndexedValues().get(10).getValue();
        return typedResponse;
    }

    public Flowable<SafeMultiSigTransactionEventResponse> safeMultiSigTransactionEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSafeMultiSigTransactionEventFromLog(log));
    }

    public Flowable<SafeMultiSigTransactionEventResponse> safeMultiSigTransactionEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SAFEMULTISIGTRANSACTION_EVENT));
        return safeMultiSigTransactionEventFlowable(filter);
    }

    public static List<SafeReceivedEventResponse> getSafeReceivedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SAFERECEIVED_EVENT, transactionReceipt);
        ArrayList<SafeReceivedEventResponse> responses = new ArrayList<SafeReceivedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SafeReceivedEventResponse typedResponse = new SafeReceivedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.sender = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SafeReceivedEventResponse getSafeReceivedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SAFERECEIVED_EVENT, log);
        SafeReceivedEventResponse typedResponse = new SafeReceivedEventResponse();
        typedResponse.log = log;
        typedResponse.sender = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<SafeReceivedEventResponse> safeReceivedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSafeReceivedEventFromLog(log));
    }

    public Flowable<SafeReceivedEventResponse> safeReceivedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SAFERECEIVED_EVENT));
        return safeReceivedEventFlowable(filter);
    }

    public static List<SafeSetupEventResponse> getSafeSetupEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SAFESETUP_EVENT, transactionReceipt);
        ArrayList<SafeSetupEventResponse> responses = new ArrayList<SafeSetupEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SafeSetupEventResponse typedResponse = new SafeSetupEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.initiator = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.owners = (List<String>) ((Array) eventValues.getNonIndexedValues().get(0)).getNativeValueCopy();
            typedResponse.threshold = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.initializer = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.fallbackHandler = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SafeSetupEventResponse getSafeSetupEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SAFESETUP_EVENT, log);
        SafeSetupEventResponse typedResponse = new SafeSetupEventResponse();
        typedResponse.log = log;
        typedResponse.initiator = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owners = (List<String>) ((Array) eventValues.getNonIndexedValues().get(0)).getNativeValueCopy();
        typedResponse.threshold = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.initializer = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.fallbackHandler = (String) eventValues.getNonIndexedValues().get(3).getValue();
        return typedResponse;
    }

    public Flowable<SafeSetupEventResponse> safeSetupEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSafeSetupEventFromLog(log));
    }

    public Flowable<SafeSetupEventResponse> safeSetupEventFlowable(DefaultBlockParameter startBlock,
                                                                   DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SAFESETUP_EVENT));
        return safeSetupEventFlowable(filter);
    }

    public static List<SignMsgEventResponse> getSignMsgEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SIGNMSG_EVENT, transactionReceipt);
        ArrayList<SignMsgEventResponse> responses = new ArrayList<SignMsgEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            SignMsgEventResponse typedResponse = new SignMsgEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.msgHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SignMsgEventResponse getSignMsgEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SIGNMSG_EVENT, log);
        SignMsgEventResponse typedResponse = new SignMsgEventResponse();
        typedResponse.log = log;
        typedResponse.msgHash = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<SignMsgEventResponse> signMsgEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSignMsgEventFromLog(log));
    }

    public Flowable<SignMsgEventResponse> signMsgEventFlowable(DefaultBlockParameter startBlock,
                                                               DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SIGNMSG_EVENT));
        return signMsgEventFlowable(filter);
    }

    public RemoteFunctionCall<String> version() {
        final Function function = new Function(FUNC_VERSION,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> addOwnerWithThreshold(String owner,
                                                                        BigInteger threshold) {
        final Function function = new Function(
                FUNC_ADDOWNERWITHTHRESHOLD,
                Arrays.<Type>asList(new Address(160, owner),
                        new Uint256(threshold)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> approveHash(byte[] hashToApprove) {
        final Function function = new Function(
                FUNC_APPROVEHASH,
                Arrays.<Type>asList(new Bytes32(hashToApprove)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> approvedHashes(String param0, byte[] param1) {
        final Function function = new Function(FUNC_APPROVEDHASHES,
                Arrays.<Type>asList(new Address(160, param0),
                        new Bytes32(param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> changeThreshold(BigInteger threshold) {
        final Function function = new Function(
                FUNC_CHANGETHRESHOLD,
                Arrays.<Type>asList(new Uint256(threshold)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> disableModule(String prevModule, String module) {
        final Function function = new Function(
                FUNC_DISABLEMODULE,
                Arrays.<Type>asList(new Address(160, prevModule),
                        new Address(160, module)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> domainSeparator() {
        final Function function = new Function(FUNC_DOMAINSEPARATOR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> enableModule(String module) {
        final Function function = new Function(
                FUNC_ENABLEMODULE,
                Arrays.<Type>asList(new Address(160, module)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<byte[]> encodeTransactionData(String to, BigInteger value,
                                                            byte[] data, BigInteger operation, BigInteger safeTxGas, BigInteger baseGas,
                                                            BigInteger gasPrice, String gasToken, String refundReceiver, BigInteger nonce) {
        final Function function = new Function(FUNC_ENCODETRANSACTIONDATA,
                Arrays.<Type>asList(new Address(160, to),
                        new Uint256(value),
                        new DynamicBytes(data),
                        new Uint8(operation),
                        new Uint256(safeTxGas),
                        new Uint256(baseGas),
                        new Uint256(gasPrice),
                        new Address(160, gasToken),
                        new Address(160, refundReceiver),
                        new Uint256(nonce)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<TransactionReceipt> execTransaction(String to, BigInteger value,
                                                                  byte[] data, BigInteger operation, BigInteger safeTxGas, BigInteger baseGas,
                                                                  BigInteger gasPrice, String gasToken, String refundReceiver, byte[] signatures,
                                                                  BigInteger weiValue) {
        final Function function = new Function(
                FUNC_EXECTRANSACTION,
                Arrays.<Type>asList(new Address(160, to),
                        new Uint256(value),
                        new DynamicBytes(data),
                        new Uint8(operation),
                        new Uint256(safeTxGas),
                        new Uint256(baseGas),
                        new Uint256(gasPrice),
                        new Address(160, gasToken),
                        new Address(160, refundReceiver),
                        new DynamicBytes(signatures)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> execTransactionFromModule(String to,
                                                                            BigInteger value, byte[] data, BigInteger operation) {
        final Function function = new Function(
                FUNC_EXECTRANSACTIONFROMMODULE,
                Arrays.<Type>asList(new Address(160, to),
                        new Uint256(value),
                        new DynamicBytes(data),
                        new Uint8(operation)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> execTransactionFromModuleReturnData(String to,
                                                                                      BigInteger value, byte[] data, BigInteger operation) {
        final Function function = new Function(
                FUNC_EXECTRANSACTIONFROMMODULERETURNDATA,
                Arrays.<Type>asList(new Address(160, to),
                        new Uint256(value),
                        new DynamicBytes(data),
                        new Uint8(operation)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getChainId() {
        final Function function = new Function(FUNC_GETCHAINID,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple2<List<String>, String>> getModulesPaginated(String start,
                                                                                BigInteger pageSize) {
        final Function function = new Function(FUNC_GETMODULESPAGINATED,
                Arrays.<Type>asList(new Address(160, start),
                        new Uint256(pageSize)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {
                }, new TypeReference<Address>() {
                }));
        return new RemoteFunctionCall<Tuple2<List<String>, String>>(function,
                new Callable<Tuple2<List<String>, String>>() {
                    @Override
                    public Tuple2<List<String>, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<String>, String>(
                                convertToNative((List<Address>) results.get(0).getValue()),
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<List> getOwners() {
        final Function function = new Function(FUNC_GETOWNERS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {
                }));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<byte[]> getStorageAt(BigInteger offset, BigInteger length) {
        final Function function = new Function(FUNC_GETSTORAGEAT,
                Arrays.<Type>asList(new Uint256(offset),
                        new Uint256(length)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<BigInteger> getThreshold() {
        final Function function = new Function(FUNC_GETTHRESHOLD,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<byte[]> getTransactionHash(String to, BigInteger value, byte[] data,
                                                         BigInteger operation, BigInteger safeTxGas, BigInteger baseGas, BigInteger gasPrice,
                                                         String gasToken, String refundReceiver, BigInteger nonce) {
        final Function function = new Function(FUNC_GETTRANSACTIONHASH,
                Arrays.<Type>asList(new Address(160, to),
                        new Uint256(value),
                        new DynamicBytes(data),
                        new Uint8(operation),
                        new Uint256(safeTxGas),
                        new Uint256(baseGas),
                        new Uint256(gasPrice),
                        new Address(160, gasToken),
                        new Address(160, refundReceiver),
                        new Uint256(nonce)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {
                }));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteFunctionCall<Boolean> isModuleEnabled(String module) {
        final Function function = new Function(FUNC_ISMODULEENABLED,
                Arrays.<Type>asList(new Address(160, module)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Boolean> isOwner(String owner) {
        final Function function = new Function(FUNC_ISOWNER,
                Arrays.<Type>asList(new Address(160, owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> nonce() {
        final Function function = new Function(FUNC_NONCE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> removeOwner(String prevOwner, String owner,
                                                              BigInteger threshold) {
        final Function function = new Function(
                FUNC_REMOVEOWNER,
                Arrays.<Type>asList(new Address(160, prevOwner),
                        new Address(160, owner),
                        new Uint256(threshold)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setFallbackHandler(String handler) {
        final Function function = new Function(
                FUNC_SETFALLBACKHANDLER,
                Arrays.<Type>asList(new Address(160, handler)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setGuard(String guard) {
        final Function function = new Function(
                FUNC_SETGUARD,
                Arrays.<Type>asList(new Address(160, guard)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setup(List<String> owners, BigInteger threshold,
                                                        String to, byte[] data, String fallbackHandler, String paymentToken, BigInteger payment,
                                                        String paymentReceiver) {
        final Function function = new Function(
                FUNC_SETUP,
                Arrays.<Type>asList(new DynamicArray<Address>(
                                Address.class,
                                org.web3j.abi.Utils.typeMap(owners, Address.class)),
                        new Uint256(threshold),
                        new Address(160, to),
                        new DynamicBytes(data),
                        new Address(160, fallbackHandler),
                        new Address(160, paymentToken),
                        new Uint256(payment),
                        new Address(160, paymentReceiver)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> signedMessages(byte[] param0) {
        final Function function = new Function(FUNC_SIGNEDMESSAGES,
                Arrays.<Type>asList(new Bytes32(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> simulateAndRevert(String targetContract,
                                                                    byte[] calldataPayload) {
        final Function function = new Function(
                FUNC_SIMULATEANDREVERT,
                Arrays.<Type>asList(new Address(160, targetContract),
                        new DynamicBytes(calldataPayload)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> swapOwner(String prevOwner, String oldOwner,
                                                            String newOwner) {
        final Function function = new Function(
                FUNC_SWAPOWNER,
                Arrays.<Type>asList(new Address(160, prevOwner),
                        new Address(160, oldOwner),
                        new Address(160, newOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SafeL2 load(String contractAddress, Web3j web3j, Credentials credentials,
                              BigInteger gasPrice, BigInteger gasLimit) {
        return new SafeL2(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SafeL2 load(String contractAddress, Web3j web3j,
                              TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SafeL2(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SafeL2 load(String contractAddress, Web3j web3j, Credentials credentials,
                              ContractGasProvider contractGasProvider) {
        return new SafeL2(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SafeL2 load(String contractAddress, Web3j web3j,
                              TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SafeL2(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class AddedOwnerEventResponse extends BaseEventResponse {
        public String owner;
    }

    public static class ApproveHashEventResponse extends BaseEventResponse {
        public byte[] approvedHash;

        public String owner;
    }

    public static class ChangedFallbackHandlerEventResponse extends BaseEventResponse {
        public String handler;
    }

    public static class ChangedGuardEventResponse extends BaseEventResponse {
        public String guard;
    }

    public static class ChangedThresholdEventResponse extends BaseEventResponse {
        public BigInteger threshold;
    }

    public static class DisabledModuleEventResponse extends BaseEventResponse {
        public String module;
    }

    public static class EnabledModuleEventResponse extends BaseEventResponse {
        public String module;
    }

    public static class ExecutionFailureEventResponse extends BaseEventResponse {
        public byte[] txHash;

        public BigInteger payment;
    }

    public static class ExecutionFromModuleFailureEventResponse extends BaseEventResponse {
        public String module;
    }

    public static class ExecutionFromModuleSuccessEventResponse extends BaseEventResponse {
        public String module;
    }

    public static class ExecutionSuccessEventResponse extends BaseEventResponse {
        public byte[] txHash;

        public BigInteger payment;
    }

    public static class RemovedOwnerEventResponse extends BaseEventResponse {
        public String owner;
    }

    public static class SafeModuleTransactionEventResponse extends BaseEventResponse {
        public String module;

        public String to;

        public BigInteger value;

        public byte[] data;

        public BigInteger operation;
    }

    public static class SafeMultiSigTransactionEventResponse extends BaseEventResponse {
        public String to;

        public BigInteger value;

        public byte[] data;

        public BigInteger operation;

        public BigInteger safeTxGas;

        public BigInteger baseGas;

        public BigInteger gasPrice;

        public String gasToken;

        public String refundReceiver;

        public byte[] signatures;

        public byte[] additionalInfo;
    }

    public static class SafeReceivedEventResponse extends BaseEventResponse {
        public String sender;

        public BigInteger value;
    }

    public static class SafeSetupEventResponse extends BaseEventResponse {
        public String initiator;

        public List<String> owners;

        public BigInteger threshold;

        public String initializer;

        public String fallbackHandler;
    }

    public static class SignMsgEventResponse extends BaseEventResponse {
        public byte[] msgHash;
    }
}
