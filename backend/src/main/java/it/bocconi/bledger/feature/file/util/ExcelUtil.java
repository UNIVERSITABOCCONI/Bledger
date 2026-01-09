package it.bocconi.bledger.feature.file.util;


import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ExcelUtil {

    private static final String UNIT_OF_MEASURE = "(tCO\u2082e)";

    private ExcelUtil() {
        // Utility class, no instantiation
    }

    public static Mono<byte[]> createSummaryExcelMono(List<NodeWithCompanyNameRow> bcNodes, BcNode currentNode) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> createSummaryExcel(bcNodes, currentNode));
    }

    public static Flux<BcCompany> getCompaniesAndRolesFlux(Map<String, String> fileMapping, Resource resource) {
        return ReactiveUtils.scheduleOnBundleElasticFlux(() -> Flux.fromIterable(extractCompanies(fileMapping, resource)));
    }

    @NotNull
    private static byte[] createSummaryExcel(List<NodeWithCompanyNameRow> bcNodes, BcNode currentNode) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String currentNodeId = currentNode.getId();


            CellStyle headerStyle = getHeaderStyle(workbook);

            createMyEmissionDataSheet(currentNode, workbook, headerStyle);
            createRelativesSheet(bcNodes, workbook, headerStyle, currentNodeId);


            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel summary", e);
        }
    }


    private static void createMyEmissionDataSheet(BcNode currentNode, Workbook workbook, CellStyle headerStyle) {
        Sheet emissionsSheet = workbook.createSheet("My Emission Data");
        Row header = emissionsSheet.createRow(0);

        String[] headers = {
                "Emission Intensity " + UNIT_OF_MEASURE,
                "Production Volume",
                "Scope1 " + UNIT_OF_MEASURE,
                "Scope2 " + UNIT_OF_MEASURE,
                "Scope3 " + UNIT_OF_MEASURE
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Row row = emissionsSheet.createRow(1);

        row.createCell(0).setCellValue(valueOrNull(currentNode.getEValue()));
        row.createCell(1).setCellValue(valueOrNull(currentNode.getProductionVolume()));
        row.createCell(2).setCellValue(valueOrNull(currentNode.getScope1()));
        row.createCell(3).setCellValue(valueOrNull(currentNode.getScope2()));
        row.createCell(4).setCellValue(valueOrNull(currentNode.getScope3()));

        for (int i = 0; i < headers.length; i++) {
            emissionsSheet.setColumnWidth(i, 256*27);
        }

    }

    private static void createRelativesSheet(List<NodeWithCompanyNameRow> bcNodes, Workbook workbook, CellStyle headerStyle, String currentNodeId) {
        Sheet relativesSheet = workbook.createSheet("Suppliers summary");

        Row header = relativesSheet.createRow(0);
        String[] headers = {
                "Company name",
                "Scope1 " + UNIT_OF_MEASURE,
                "Scope2 " + UNIT_OF_MEASURE,
                "Total Scope1 and Scope2 " + UNIT_OF_MEASURE,
                "q",
                "t "+ UNIT_OF_MEASURE,
                "Emission Intensity" + UNIT_OF_MEASURE
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (NodeWithCompanyNameRow node : bcNodes) {

            Row row = relativesSheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(node.getCompanyName() != null ? node.getCompanyName() : "");
            row.createCell(1).setCellValue(node.getScope1() != null ? node.getScope1() : "");
            row.createCell(2).setCellValue(node.getScope2() != null ? node.getScope2() : "");
            row.createCell(3).setCellValue(node.getTotalScope1AndScope2() != null ? node.getTotalScope1AndScope2() : "");
            row.createCell(4).setCellValue(valueOrNull(node.getQuantity()));
            row.createCell(5).setCellValue(valueOrNull(node.getTransportationEmission()));
            row.createCell(6).setCellValue(valueOrNull(node.getEValue()));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            relativesSheet.setColumnWidth(i, 256*27);
        }
    }

    @NotNull
    private static CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        if (workbook instanceof XSSFWorkbook) {
            XSSFColor whiteColor = new XSSFColor(java.awt.Color.WHITE, null);
            ((XSSFFont) headerFont).setColor(whiteColor);
        } else {
            headerFont.setColor(IndexedColors.WHITE.getIndex());
        }

        headerStyle.setFont(headerFont);

        if (workbook instanceof XSSFWorkbook) {
            XSSFColor headerColor = new XSSFColor(java.awt.Color.decode("#0046AE"), null);
            ((XSSFCellStyle) headerStyle).setFillForegroundColor(headerColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else {
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return headerStyle;
    }


    @NotNull
    private static List<BcCompany> extractCompanies(Map<String, String> fileMapping, Resource resource) {
        try (InputStream is = resource.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<BcCompany> companies = new ArrayList<>();

            boolean headerSkipped = false;
            for (Row row : sheet) {
                if (!headerSkipped) { headerSkipped = true; continue; }

                String companyId = cell(row, 0);
                BcCompany company = BcCompany.builder()
                        .email(cell(row, 1))
                        .nation(cell(row, 2))
                        .companyName(cell(row, 3))
                        .idType(cell(row, 4))
                        .idNumber(cell(row, 5))
                        .representativeName(cell(row, 6))
                        .representativeSurname(cell(row, 7))
                        .profileImageId(fileMapping.getOrDefault(companyId, null))
                        .companyType(CompanyType.valueOf(cell(row, 8)))
                        .build();
                company.prePersist();

                companies.add(company);
            }
            return companies;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String cell(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING    -> cell.getStringCellValue().trim();
            case NUMERIC   -> (DateUtil.isCellDateFormatted(cell))
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN   -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA   -> cell.getCellFormula();
            default        -> null;
        };
    }

    private static String valueOrNull(Object o) {
        return o == null ? "" : o.toString();
    }

}
