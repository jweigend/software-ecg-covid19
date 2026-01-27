package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.smoothing;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for the SumTimeSeriesSmoother implementation that will
 * challenge the slicing computation in the tested class that
 * is used to create buckets, assign the series values to it and sum it's values.
 */
public class SumTimeSeriesSmootherTest {

    private static final String TEST_DATA_FILE = "/de/qaware/ekg/bl/services/value-testdata.csv";
    private static final String AGG_TEST_DATA_FILE = "/de/qaware/ekg/bl/services/value-testdata-month-agg.csv";

    private static final long OFFSET_TWO_MONTHS = 2 * 2_628_000_000L;

    private static final long ONE_DAY = 86_400_000L;

    private static final long ONE_MONTH = 2_628_000_000L;

    private static final long HALF_MONTH = 2_628_000_000L / 2;


    @Test
    public void testOneYearSimpleGeneratedMonthAgg() {
        SumTimeSeriesSmoother smoother = new SumTimeSeriesSmoother(SeriesSmoothingGranularity.MONTH);

        List<Value> result = smoother.computeSmoothing(generateOneYearValues(false));

        assertNotNull(result);
        assertEquals(12, result.size());

        assertEquals(60.0, result.get(0).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + HALF_MONTH, result.get(0).getTimestamp(), 0);

        assertEquals(60.0, result.get(1).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + ONE_MONTH + HALF_MONTH, result.get(1).getTimestamp(), 0);

        assertEquals(60.0, result.get(2).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + 2 * ONE_MONTH + HALF_MONTH, result.get(2).getTimestamp(), 0);

        assertEquals(60.0, result.get(3).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + 3 * ONE_MONTH + HALF_MONTH, result.get(3).getTimestamp(), 0);

        assertEquals(60.0, result.get(10).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + 10 * ONE_MONTH + HALF_MONTH, result.get(10).getTimestamp(), 0);

        assertEquals(60.0, result.get(11).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + 11 * ONE_MONTH + HALF_MONTH, result.get(11).getTimestamp(), 0);

        List<Value> modValueList = generateOneYearValues(true);
        double totalModValue = modValueList.stream().map(Value::getValue).reduce((d1, d2) -> d1 + d2).get();

        List<Value> result2 = smoother.computeSmoothing(modValueList);
        double totalSmoothed = result2.stream().map(Value::getValue).reduce((d1, d2) -> d1 + d2).get();

        assertNotNull(result2);
        assertEquals(12, result2.size());
        assertEquals(totalModValue, totalSmoothed, 0);

        assertEquals(87.0, result2.get(0).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + HALF_MONTH, result2.get(0).getTimestamp(), 0);

        assertEquals(91, result2.get(1).getValue(), 2);
        assertEquals(OFFSET_TWO_MONTHS + ONE_MONTH + HALF_MONTH, result2.get(1).getTimestamp(), 0);
    }

    @Test
    public void testOneYearRealTestdataMonthAgg() throws Exception {
        SumTimeSeriesSmoother smoother = new SumTimeSeriesSmoother(SeriesSmoothingGranularity.MONTH);

        List<Value> testData = readInTestdata(AGG_TEST_DATA_FILE);
        double totalTestData = testData.stream().map(Value::getValue).reduce((d1, d2) -> d1 + d2).get();

        List<Value> result = smoother.computeSmoothing(readInTestdata(TEST_DATA_FILE));
        double totalSmoothedData = result.stream().map(Value::getValue).reduce((d1, d2) -> d1 + d2).get();

        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(totalTestData, totalSmoothedData, 0);

        assertEquals(getMonthIntervalSum(0, testData), result.get(0).getValue(), 0.1);
        assertEquals(1653.75, result.get(1).getValue(), 0.1);
        assertEquals(1696.75, result.get(2).getValue(), 0.1);
        assertEquals(1642.0, result.get(3).getValue(), 0.1);
        assertEquals(1542.5, result.get(4).getValue(), 0.1);
        assertEquals(1914.75, result.get(5).getValue(), 0.1);
        assertEquals(1877.15, result.get(6).getValue(), 0.1);
        assertEquals(1645.5, result.get(7).getValue(), 0.1);
        assertEquals(1747.25, result.get(8).getValue(), 0.1);
        assertEquals(1910.25, result.get(9).getValue(), 0.1);
        assertEquals(2054.15, result.get(10).getValue(), 0.1);
        assertEquals(1452, result.get(11).getValue(), 0.1);
    }

    @Test
    public void testOneYearRealTestdataCalendarMonthAgg() throws Exception {
        SumTimeSeriesSmoother smoother = new SumTimeSeriesSmoother(SeriesSmoothingGranularity.CALENDAR_MONTH);

        List<Value> result = smoother.computeSmoothing(readInTestdata(TEST_DATA_FILE));

        assertNotNull(result);
        assertEquals(12, result.size());

        assertEquals(1276.25, result.get(0).getValue(), 0.1);
        assertEquals(1500.50, result.get(1).getValue(), 0.1);
        assertEquals(1843.75, result.get(2).getValue(), 0.1);
        assertEquals(1551.75, result.get(3).getValue(), 0.1);
        assertEquals(1592.00, result.get(4).getValue(), 0.1);
        assertEquals(1853.00, result.get(5).getValue(), 0.1);
        assertEquals(1899.65, result.get(6).getValue(), 0.1);
        assertEquals(1731.75, result.get(7).getValue(), 0.1);
        assertEquals(1648.50, result.get(8).getValue(), 0.1);
        assertEquals(2007.00, result.get(9).getValue(), 0.1);
        assertEquals(2056.15, result.get(10).getValue(), 0.1);
        assertEquals(1452.00, result.get(11).getValue(), 0.1);

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:SS");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals(df.parse("16.01.2018 00:00:00").getTime(), result.get(0).getTimestamp(), 0);
        assertEquals(df.parse("15.02.2018 00:00:00").getTime(), result.get(1).getTimestamp(), 0);
        assertEquals(df.parse("16.03.2018 00:00:00").getTime(), result.get(2).getTimestamp(), 0);
        assertEquals(df.parse("16.04.2018 00:00:00").getTime(), result.get(3).getTimestamp(), 0);
        assertEquals(df.parse("16.05.2018 00:00:00").getTime(), result.get(4).getTimestamp(), 0);
        assertEquals(df.parse("16.06.2018 00:00:00").getTime(), result.get(5).getTimestamp(), 0);
        assertEquals(df.parse("16.07.2018 00:00:00").getTime(), result.get(6).getTimestamp(), 0);
        assertEquals(df.parse("16.08.2018 00:00:00").getTime(), result.get(7).getTimestamp(), 0);
        assertEquals(df.parse("16.09.2018 00:00:00").getTime(), result.get(8).getTimestamp(), 0);
        assertEquals(df.parse("16.10.2018 00:00:00").getTime(), result.get(9).getTimestamp(), 0);
        assertEquals(df.parse("16.11.2018 00:00:00").getTime(), result.get(10).getTimestamp(), 0);
        assertEquals(df.parse("16.12.2018 00:00:00").getTime(), result.get(11).getTimestamp(), 0);
    }


    //----------------------------------------------------------------------------------------------------------------
    //  helper methods to support the tests
    //----------------------------------------------------------------------------------------------------------------


    private double getMonthIntervalSum(int startIndex, List<Value> valueList) {

        double resultValue = 0;

        int max = valueList.size() > startIndex + 31 ? startIndex + 31 : valueList.size();

        for (int i = startIndex; i < max; i++) {
            resultValue += valueList.get(i).getValue();
        }

        return resultValue;
    }

    private List<Value> readInTestdata(String file) throws Exception {

        String stringData = FileUtils.readFileToString(new File(getClass().getResource(file).toURI()), "UTF-8");

        String[] lines = stringData.split("\n");
        List<Value> values = new ArrayList<>(lines.length);

        for (String line : lines) {
            int posDelimiter = line.indexOf(';');
            long timestamp = Long.valueOf(line.substring(0, posDelimiter));
            double value = Double.valueOf(line.substring(posDelimiter + 1));

            values.add(new Value(timestamp, value));
        }

        return values;
    }


    private List<Value> generateOneYearValues(boolean useModulo) {
        List<Value> valueList = new ArrayList<>(365);

        int n = 0;

        for (int i=0; i<365; i++) {
            if (useModulo) {
                valueList.add(new Value(OFFSET_TWO_MONTHS + ONE_DAY * i, (n++ % 7)));
            } else {
                valueList.add(new Value(OFFSET_TWO_MONTHS + ONE_DAY * i, 2.0));
            }
        }

        return valueList;
    }
}
