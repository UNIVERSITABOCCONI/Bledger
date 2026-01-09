package it.bocconi.bledger.feature.file.dao.row.impl;

import it.bocconi.bledger.feature.file.dao.row.CompanyProfileImageRow;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyProfileImageRowImpl implements CompanyProfileImageRow {

    private String fileName;

    private String mimeType;

    private Long size;

    private byte[] fileBinary;
}
