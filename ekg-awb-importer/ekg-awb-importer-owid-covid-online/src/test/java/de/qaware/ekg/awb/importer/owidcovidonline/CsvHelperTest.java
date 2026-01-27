package de.qaware.ekg.awb.importer.owidcovidonline;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvHelperTest {

    private static final List<String> EXPECTED_HEADERS =  List.of("iso_code", "continent", "location", "date");
    private static final String EMPTY_ROWS_FILE = "testdata/validateFile_emptyRows.csv";
    private static final String MISSING_HEADER_FILE = "testdata/validateFile_missingHeader.csv";
    private static final String CORRECT_FILE = "testdata/validateFile_correct.csv";
    private static final String SAMPLE_FILE = "testdata/sample.csv";
    private static final String UNKNOWN_DATE_FORMAT_FILE = "testdata/unknownDateFormat.csv";
    private static final String SECOND_DATE_FORMAT_FILE = "testdata/secondDateFormat.csv";

    @Test
    void validateCsvHeaders() throws Exception {
        assertThat(CsvHelper.validateCsvHeaders(new File(ClassLoader.getSystemResource(EMPTY_ROWS_FILE).toURI()), EXPECTED_HEADERS)).isTrue();
        assertThat(CsvHelper.validateCsvHeaders(new File(ClassLoader.getSystemResource(MISSING_HEADER_FILE).toURI()), EXPECTED_HEADERS)).isFalse();
        assertThat(CsvHelper.validateCsvHeaders(new File(ClassLoader.getSystemResource(CORRECT_FILE).toURI()), EXPECTED_HEADERS)).isTrue();
    }

    @Test
    void readNewestDate() throws Exception {
        assertThat(CsvHelper.readNewestDate(new File(ClassLoader.getSystemResource(SAMPLE_FILE).toURI())))
                .isEqualTo(LocalDate.of(2020, 10, 12));
    }


    @Test
    void readNewestDate_unknownDateFormat() throws Exception {
        assertThatThrownBy(() -> CsvHelper.readNewestDate(new File(ClassLoader.getSystemResource(UNKNOWN_DATE_FORMAT_FILE).toURI())))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void readNewestDate_secondDateFormat() throws Exception {
        assertThat(CsvHelper.readNewestDate(new File(ClassLoader.getSystemResource(SECOND_DATE_FORMAT_FILE).toURI())))
                .isEqualTo(LocalDate.of(2020, 3, 26));
    }

}