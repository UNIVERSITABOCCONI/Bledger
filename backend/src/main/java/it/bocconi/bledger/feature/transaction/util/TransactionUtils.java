package it.bocconi.bledger.feature.transaction.util;

import it.bocconi.bledger.blockchain.util.safe.SafeL2;
import it.bocconi.bledger.feature.transaction.entity.BcTransaction;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;
import java.time.LocalDateTime;

public class TransactionUtils {

    public static BcTransaction setTransactionFields(BcTransaction transaction, TransactionReceipt transactionReceipt) {
        if (transactionReceipt == null) {
            return transaction;
        }

        transaction.setFromWalletAddress(transactionReceipt.getFrom());
        transaction.setToAddress(transactionReceipt.getTo());
        transaction.setTxHash(transactionReceipt.getTransactionHash());
        transaction.setBlockNumber(transactionReceipt.getBlockNumber());
        transaction.setGasPrice(transactionReceipt.getEffectiveGasPrice() != null ? Numeric.decodeQuantity(transactionReceipt.getEffectiveGasPrice()) : null);
        transaction.setGasUsed(transactionReceipt.getGasUsed());
        transaction.setConfirmedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.CONFIRMED);
        var multiSigTx = SafeL2.getSafeMultiSigTransactionEvents(transactionReceipt);
        if (!multiSigTx.isEmpty()) {
            transaction.setToAddress(multiSigTx.getFirst().to);
        }
        var executionFailedEvent = SafeL2.getExecutionFailureEvents(transactionReceipt);
        if (!executionFailedEvent.isEmpty()) {
            transaction.setStatus(TransactionStatus.FAILED);
        }
        return transaction;
    }
}
