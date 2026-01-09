package it.bocconi.bledger.feature.file.util;


import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private ZipUtil() {
        // Utility class, no instantiation allowed
    }
    public static final String ZIP_FILE_EXTENSION = ".zip";

    public static Mono<byte[]> exportZipMono(List<Map.Entry<NodeWithCompanyNameRow, byte[]>> entries, byte[] excelBytes) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> createExportZip(entries, excelBytes));
    }

    public static Mono<byte[]> internalZipMono(byte[] scope1, byte[] scope2, byte[] global, byte[] metadata) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> createInternalZip(scope1, scope2, global, metadata));
    }
    @NotNull
    private static byte[] createExportZip(List<Map.Entry<NodeWithCompanyNameRow, byte[]>> entries, byte[] excelBytes) {
        try (ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
             ZipOutputStream finalZip = new ZipOutputStream(finalBaos)) {

            for (Map.Entry<NodeWithCompanyNameRow, byte[]> entry : entries) {
                String zipName = entry.getKey().getCompanyName() + ZIP_FILE_EXTENSION;
                finalZip.putNextEntry(new ZipEntry(zipName));
                finalZip.write(entry.getValue());
                finalZip.closeEntry();
            }

            finalZip.putNextEntry(new ZipEntry("summary.xlsx"));
            finalZip.write(excelBytes);
            finalZip.closeEntry();

            finalZip.finish();
            return finalBaos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error creating export zip", e);
        }
    }

    @NotNull
    private static byte[] createInternalZip(byte[] scope1, byte[] scope2, byte[] global, byte[] metadata) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(baos)) {

            zip.putNextEntry(new ZipEntry("granular_scope_1_data_facts.csv"));
            zip.write(scope1);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("granular_scope_2_data_facts.csv"));
            zip.write(scope2);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("scope_1_2_data_facts.csv"));
            zip.write(global);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("metadata-bledger.json"));
            zip.write(metadata);
            zip.closeEntry();

            zip.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error creating node zip", e);
        }
    }
 
    public static Mono<CompaniesZip> readCompaniesZipMono(byte[] zipBytes) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> readCompaniesZip(zipBytes));
    }
 
    private static CompaniesZip readCompaniesZip(byte[] zipBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {
 
            List<LogoEntry> logos = new ArrayList<>();
            byte[] companiesExcel = null;
 
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().replace('\\', '/');
                String lower = name.toLowerCase();
 
                if (lower.contains("logos/")) {
                    String fileName = name.substring(name.lastIndexOf('/') + 1);
                    byte[] bytes = readAll(zis);
                    logos.add(new LogoEntry(fileName, bytes));
                } else if (lower.endsWith("companies.xlsx")) {
                    // Be tolerant with path structure (e.g., company/companies.xlsx or any */companies.xlsx)
                    companiesExcel = readAll(zis);
                }
 
                zis.closeEntry();
            }
 
            if (companiesExcel == null) {
                throw new RuntimeException("companies.xlsx not found in zip");
            }
 
            return new CompaniesZip(logos, companiesExcel);
 
        } catch (IOException e) {
            throw new RuntimeException("Error reading companies zip", e);
        }
    }
 
    private static byte[] readAll(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
 
    public record LogoEntry(String fileName, byte[] bytes) {}
    public record CompaniesZip(List<LogoEntry> logos, byte[] companiesExcel) {}
}
