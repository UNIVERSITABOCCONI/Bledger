package it.bocconi.bledger.feature.company.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_company")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcCompany extends BcAbstractEntity {

    @Column("email")
    private String email;

    @Column("nation")
    private String nation;

    @Column("company_name")
    private String companyName;

    @Column("id_type")
    private String idType;

    @Column("id_number")
    private String idNumber;

    @Column("representative_name")
    private String representativeName;

    @Column("representative_surname")
    private String representativeSurname;

    @Column("wallet_id")
    private String walletId;

    @Column("profile_image_id")
    private String profileImageId;

    @Column("company_type")
    private CompanyType companyType;
}
