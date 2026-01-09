package it.bocconi.bledger.feature.transaction.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_transaction_wallet_recipient")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcTransactionWalletRecipient extends BcAbstractEntity {

    @Column("transaction_id")
    private String transactionId;

    @Column("wallet_id")
    private String walletId;
}
