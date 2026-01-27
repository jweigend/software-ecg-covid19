package de.qaware.ekg.awb.importer.owidcovidonline;

import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Helper for validating or getting information from a CSV.
 */
public final class CsvHelper {

    private static final Logger LOGGER = EkgLogger.get();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("[yyyy-MM-dd]" + "[dd/MM/yyyy]")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    public static final LocalDate DEFAULT_DATE = LocalDate.parse("1970-01-01");

    /**
     * Expected headers for OWID COVID-19 data files.
     */
    public static final List<String> EXPECTED_HEADERS = List.of("iso_code", "continent", "location", "date");

    private CsvHelper() {
        // Hide Ctor. Class has only static methods.
    }

    /**
     * Get the newest date from the CSV file. Therefore it uses the date column and search the highest date.
     * Supports both regular CSV files and GZip-compressed CSV files (.csv.gz).
     *
     * @param file The file which should be scanned for the highest date.
     * @return The newest date or if not found the date of 01.01.1970;
     */
    public static LocalDate readNewestDate(File file) {
        try {
            CsvReader csvReader = new CsvReader();
            csvReader.setFieldSeparator(',');
            csvReader.setContainsHeader(true);
            
            CsvContainer csvContainer;
            if (file.getName().endsWith(".gz")) {
                // Read from GZip-compressed file
                try (FileInputStream fis = new FileInputStream(file);
                     GZIPInputStream gzis = new GZIPInputStream(fis);
                     InputStreamReader reader = new InputStreamReader(gzis, StandardCharsets.UTF_8)) {
                    csvContainer = csvReader.read(reader);
                }
            } else {
                csvContainer = csvReader.read(file, StandardCharsets.UTF_8);
            }
            
            Optional<LocalDate> newestDate = csvContainer.getRows().stream()
                    .map(r -> r.getField("date"))
                    .map(d -> LocalDate.from(DATE_FORMATTER.parse(d)))
                    .max(LocalDate::compareTo);

            return newestDate.orElse(DEFAULT_DATE);
        } catch (IOException e) {
            LOGGER.error("Could not read the newest date from the CSV", e);
            return DEFAULT_DATE;
        }
    }

    /**
     * Validating the given csv, that it contains the expected headers.
     * Supports both regular CSV files and GZip-compressed CSV files (.csv.gz).
     *
     * @param file            The csv file to check.
     * @param expectedHeaders A list of expected headers.
     * @return <code>True</code> if the file is valid, otherwise <code>false</code>.
     */
    public static boolean validateCsvHeaders(File file, List<String> expectedHeaders) {
        try {
            CsvReader csvReader = new CsvReader();
            csvReader.setFieldSeparator(',');
            csvReader.setContainsHeader(true);
            
            CsvParser csvParser;
            GZIPInputStream gzis = null;
            FileInputStream fis = null;
            
            try {
                if (file.getName().endsWith(".gz")) {
                    // Parse from GZip-compressed file
                    fis = new FileInputStream(file);
                    gzis = new GZIPInputStream(fis);
                    InputStreamReader reader = new InputStreamReader(gzis, StandardCharsets.UTF_8);
                    csvParser = csvReader.parse(reader);
                } else {
                    csvParser = csvReader.parse(file, StandardCharsets.UTF_8);
                }
                
                // read the header row
                csvParser.nextRow();
                if (csvParser.getHeader() == null || !csvParser.getHeader().containsAll(expectedHeaders)) {
                    LOGGER.error("The expected headers have changed. We expected {}, but the following headers are available {}", expectedHeaders, csvParser.getHeader());
                    return false;
                }
                return true;
            } finally {
                if (gzis != null) gzis.close();
                if (fis != null) fis.close();
            }
        } catch (IOException e) {
            LOGGER.error("Checking of valid CSV-File failed", e);
            return false;
        }
    }

    /**
     * Validating the given csv file path, that it contains the expected OWID COVID-19 headers.
     *
     * @param filePath The path to the csv file to check.
     * @return <code>True</code> if the file is valid, otherwise <code>false</code>.
     */
    public static boolean validateCsvHeaders(String filePath) {
        return validateCsvHeaders(new File(filePath), EXPECTED_HEADERS);
    }
}
