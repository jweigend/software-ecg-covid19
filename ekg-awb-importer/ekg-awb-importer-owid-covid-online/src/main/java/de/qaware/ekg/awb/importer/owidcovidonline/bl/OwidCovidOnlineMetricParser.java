package de.qaware.ekg.awb.importer.owidcovidonline.bl;

import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.RawSeriesData;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Parser that knows the data structure raw metric data in the source files
 * and transform it to the EKG compatible RawSeriesData format.
 */
public final class OwidCovidOnlineMetricParser {
    private static final Logger LOGGER = EkgLogger.get();

    private static final Pattern NUMBER_CHECK_PATTERN = Pattern.compile("[0-9,\\\\.]+");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final int NUMBER_OF_COLUMNS = 4;
    private static final int CONTINENT_COLUMN = 1;
    private static final int COUNTRY_COLUMN = 2;
    private static final int DATE_COLUMN = 3;

    private OwidCovidOnlineMetricParser() {
        // Hide ctor. This is a static class.
    }

    /**
     * Parse the given CSV file to a Stream of RawSeriesData.
     * Supports both regular CSV files and GZip-compressed CSV files (.csv.gz).
     *
     * @param inputFile OWID csv data file with COVID-19 statistics
     * @return a Stream of RawSeriesData that represents the payload in the log file
     */
    public static Stream<RawSeriesData> parseToRawSeriesStream(File inputFile) {

        StopWatch watch = StopWatch.createStarted();

        final List<RawSeriesData> resultList = new ArrayList<>();

        CsvReader csvReader = new CsvReader();
        csvReader.setFieldSeparator(',');

        try {
            CsvParser csvParser;
            InputStream inputStream = null;
            GZIPInputStream gzipInputStream = null;
            InputStreamReader reader = null;
            
            try {
                if (inputFile.getName().endsWith(".gz")) {
                    // Parse from GZip-compressed file
                    inputStream = new FileInputStream(inputFile);
                    gzipInputStream = new GZIPInputStream(inputStream);
                    reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                    csvParser = csvReader.parse(reader);
                } else {
                    csvParser = csvReader.parse(inputFile, StandardCharsets.UTF_8);
                }

                String[] columnNames = parseHeader(csvParser);

                int parsedCounterValues = readValuesToSeries(csvParser, columnNames, resultList);

                if (parsedCounterValues == 0) {
                    LOGGER.warn("Couldn't read any COVID statistic values from '{}'. It seems to be empty.", inputFile);
                } else {
                    LOGGER.info("Read {} counter values for {} metrics from file '{}' in {}", parsedCounterValues, columnNames.length, inputFile, watch);
                }
            } finally {
                if (reader != null) reader.close();
                if (gzipInputStream != null) gzipInputStream.close();
                if (inputStream != null) inputStream.close();
            }

        } catch (IOException | ParseException | NumberFormatException e) {
            LOGGER.error("Unexpected error occurred", e);
        }

        return resultList.stream();
    }

    @SuppressWarnings("java:S3824") // use of Map.containsKey() looks very clean, no need to change to Map.computeIfAbsent()
    private static int readValuesToSeries(CsvParser csvParser, String[] columnNames, List<RawSeriesData> resultList)
            throws IOException, ParseException, NumberFormatException {

        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

        Map<String, Map<String, RawSeriesData>> seriesDataMap = new HashMap<>();

        int lineCounter = 0;
        int columnCount = columnNames.length;

        CsvRow row;
        while ((row = csvParser.nextRow()) != null) {

            long timestamp = DATE_FORMAT.parse(row.getField(DATE_COLUMN)).getTime();
            String continent = row.getField(CONTINENT_COLUMN);
            String country = row.getField(COUNTRY_COLUMN);

            if (continent.isBlank() && ("world".equalsIgnoreCase(country) || "international".equalsIgnoreCase(country))) {
                continent = "Non-Continent";
            }

            if (!seriesDataMap.containsKey(country)) {
                Map<String, RawSeriesData> seriesData = initializeSeriesData(columnNames, continent, country);
                seriesDataMap.put(country, seriesData);
                resultList.addAll(seriesData.values());
            }

            for (int columnIdx = NUMBER_OF_COLUMNS; columnIdx < columnCount; columnIdx++) {
                String metricName = columnNames[columnIdx];

                if (metricName.contains("_cases")) {
                    seriesDataMap.get(country).get(metricName).addLabel(FilterDimension.METRIC_GROUP, "CASES");
                } else if (metricName.contains("_deaths")) {
                    seriesDataMap.get(country).get(metricName).addLabel(FilterDimension.METRIC_GROUP, "DEATHS");
                } else if (metricName.contains("tests") || metricName.contains("positive_rate")) {
                    seriesDataMap.get(country).get(metricName).addLabel(FilterDimension.METRIC_GROUP, "TESTS");
                } else {
                    seriesDataMap.get(country).get(metricName).addLabel(FilterDimension.METRIC_GROUP, "MISC");
                }

                String value = row.getField(columnIdx);

                if (StringUtils.isNotBlank(value) && NUMBER_CHECK_PATTERN.matcher(value).matches()) {
                    Number number = numberFormat.parse(value);
                    seriesDataMap.get(country).get(metricName).addValue(new Value(timestamp, number.doubleValue()));
                }
            }
            lineCounter++;
        }

        return lineCounter;
    }

    /**
     * Creates and initialize an map of RawSeriesData.
     * For each metric/column name that contains numeric values a RawSeriesData instance will created.
     * <p>
     * The returned map use the column-name as
     *
     * @param metricNames an array of metric names the RawSeriesData array should created for
     * @param continent   the name of the continent the country belongs to in english language
     * @param country     the name of the country in english language
     * @return and initialized map of RawSeriesData
     */
    private static Map<String, RawSeriesData> initializeSeriesData(String[] metricNames, String continent, String country) {

        Map<String, RawSeriesData> seriesDataMap = new HashMap<>();

        for (int i = 1; i < metricNames.length; i++) {
            String columnName = metricNames[i];
            if ("date".equals(columnName) || "iso_code".equals(columnName)
                    || "continent".equals(columnName) || "location".equals(columnName)) {
                continue;
            }

            RawSeriesData seriesData = new RawSeriesData(metricNames[i]);
            seriesData.addLabel(FilterDimension.MEASUREMENT, "-");
            seriesData.addLabel(FilterDimension.HOST_GROUP, continent);
            seriesData.addLabel(FilterDimension.HOST, country);
            seriesData.addLabel(FilterDimension.PROCESS, "-");

            seriesDataMap.put(columnName, seriesData);
        }

        return seriesDataMap;
    }

    /**
     * Parse the header fields in the first line of the CSV file
     * To be a valid collector csv the first field have to be 'Date' and all other must not be blank.
     *
     * @param csvParser the parse which provides the first line of the CSV file
     * @return an String array with metric name which doesn't include 'Date' because this isn't a metric name
     * @throws IOException    if the file couldn't read correctly for IO reasons
     * @throws ParseException if there are any validation errors inside the header line
     */
    private static String[] parseHeader(CsvParser csvParser) throws IOException, ParseException {
        CsvRow csvRow = csvParser.nextRow();

        if (csvRow == null) {
            return new String[0];
        }

        int count = csvRow.getFieldCount();
        Set<String> headerSet = new HashSet<>(count, 1f);
        String[] headers = new String[count];

        for (int i = 0; i < count; i++) {

            String headerField = csvRow.getField(i);

            if (StringUtils.isBlank(headerField)) {
                throw new ParseException("Find blank headers fields in the first line!", 0);
            }

            headers[i] = headerField;

            if (headerSet.contains(headerField)) {
                throw new ParseException("Find duplicated headers ('" + headerField + "') in the first line!", 0);
            }

            headerSet.add(headerField);
        }

        return headers;
    }
}