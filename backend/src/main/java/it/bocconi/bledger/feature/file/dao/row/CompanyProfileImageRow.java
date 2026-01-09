package it.bocconi.bledger.feature.file.dao.row;

public interface CompanyProfileImageRow {

    String getFileName();

    String getMimeType();

    Long getSize();

    byte[] getFileBinary();
}
