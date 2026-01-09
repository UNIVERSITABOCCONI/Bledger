package it.bocconi.bledger.feature.transaction.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.transaction.enums.TransactionStatus;
import it.bocconi.bledger.feature.transaction.enums.TransactionType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Table("bc_transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcTransaction extends BcAbstractEntity {

    @Column("tx_hash")
    private String txHash;

    @Column("creator_id")
    private String creatorId;

    @Column("from_wallet_id")
    private String fromWalletId;

    @Column("from_wallet_address")
    private String fromWalletAddress;

    @Column("to_address")
    private String toAddress;

    @Column("gas_price")
    private BigInteger gasPrice;

    @Column("gas_used")
    private BigInteger gasUsed;

    @Column("block_number")
    private BigInteger blockNumber;

    @Column("status")
    private TransactionStatus status;

    @Column("payload")
    private String payload;

    @Column("transaction_data")
    private String transactionData;

    @Column("ready_at")
    private LocalDateTime readyAt;

    @Column("try_count")
    @Builder.Default
    private Integer tryCount = 0;

    @Column("confirmed_at")
    private LocalDateTime confirmedAt;

    @Column("transaction_type")
    private TransactionType transactionType;

    @Column("network_id")
    private String networkId;

    @Column("node_id")
    private String nodeId;

    @Column("transaction_receipt")
    private String transactionReceipt;
}
