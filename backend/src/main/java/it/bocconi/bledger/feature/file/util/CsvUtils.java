package it.bocconi.bledger.feature.file.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

public class CsvUtils {

    private CsvUtils() {
        // Utility class, no instantiation allowed
    }

    public static Mono<String> convertCsvToJsonMono(String csvContent, List<String> customHeaders) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> convertCsvToJson(csvContent, customHeaders));
    }

    public static Mono<String> convertGlobalFileToJsonMono(String csvContent) {
        return ReactiveUtils.scheduleOnBundleElasticMono(() -> convertGlobalFileInJson(csvContent));
    }

    public static String convertCsvToJson(String csvContent, List<String> customHeaders) {
        try {
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader(customHeaders.toArray(new String[0]))
                    .setSkipHeaderRecord(true)
                    .get();

            try (CSVParser parser = CSVParser.parse(csvContent, format)) {
                List<Map<String, String>> records = new ArrayList<>();
                for (CSVRecord record : parser) {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (String header : customHeaders) {
                        map.put(header, record.get(header));
                    }
                    records.add(map);
                }
                return new ObjectMapper().writeValueAsString(records);
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV parsing failed", e);
        }
    }

    public static String convertGlobalFileInJson(String csvContent) {
        try (CSVParser parser = CSVParser.parse(csvContent, CSVFormat.DEFAULT)) {

            Iterator<CSVRecord> it = parser.iterator();
            if (!it.hasNext()) {
                return "[]";
            }

            CSVRecord componentsRow = it.next();

            if (!it.hasNext()) {
                throw new IllegalArgumentException(
                        "CSV must contain at least two rows: components (row 1) and totals (row 2)."
                );
            }

            CSVRecord totalsRow = it.next();

            List<Map<String, String>> out = new ArrayList<>();

            int cols = Math.max(componentsRow.size(), totalsRow.size());
            for (int i = 0; i < cols; i++) {
                String component = i < componentsRow.size() ? componentsRow.get(i).trim() : "";

                String total = i < totalsRow.size() ? totalsRow.get(i).trim() : "";

                Map<String, String> item = new LinkedHashMap<>();
                item.put("scope_1_2_component", component);
                item.put("unit", "utr:gCO2e");
                item.put("total_co2e", total);

                out.add(item);
            }

            return new ObjectMapper().writeValueAsString(out);

        } catch (IOException e) {
            throw new RuntimeException("CSV parsing failed", e);
        }
    }


}
