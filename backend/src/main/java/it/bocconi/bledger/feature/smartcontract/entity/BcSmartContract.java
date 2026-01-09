package it.bocconi.bledger.feature.smartcontract.entity;

import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "bc_smart_contract")
@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BcSmartContract extends BcAbstractEntity {

    @Column("contract_address")
    private String contractAddress;
    @Column("type")
    private SmartContractType type;

}