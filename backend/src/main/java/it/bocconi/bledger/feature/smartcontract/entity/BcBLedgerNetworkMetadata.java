package it.bocconi.bledger.feature.smartcontract.entity;

import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "bc_b_ledger_network_metadata")
@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BcBLedgerNetworkMetadata extends BcAbstractEntity {
    private String networkId;
    private String contentHash;
    private String content;
}
