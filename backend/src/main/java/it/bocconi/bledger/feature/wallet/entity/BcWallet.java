package it.bocconi.bledger.feature.wallet.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.transaction.enums.WalletType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Table("bc_wallet")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcWallet extends BcAbstractEntity {

    private String walletAddress;

    private BigDecimal balance;

    @Builder.Default
    private Boolean toDeploy = true;

    @Builder.Default
    private Boolean deployed = false;

    private BigInteger salt;

    @Builder.Default
    private WalletType walletType = WalletType.COMPANY;

    private LocalDateTime deployedAt;

    @Builder.Default
    private Boolean isWhitelisted = false;

}
