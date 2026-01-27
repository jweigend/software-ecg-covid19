package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.ClassicTimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for the SumCombiner class that merges time series
 * by sum the values on same time interval
 */
public class SumCombinerTest {

    @Test
    @Ignore("TODO: fix it")
    public void testCombine() {

        SumCombiner combiner = new SumCombiner(SeriesCombineMode.SUM_EXACT);

        List<TimeSeries> inSeries = createTimeSeriesList();
        TimeSeries resultSeries = combiner.combine("mySeries", inSeries);

        // compare meta data
        assertNotNull(resultSeries);
        assertEquals("mySeries", resultSeries.getMetricName());
        assertEquals("project", resultSeries.getProject());
        assertEquals("*", resultSeries.getHostGroup());

        // compare values
        List<Value> values = resultSeries.getValues();
        assertEquals(3, resultSeries.size());
        assertEquals(6, values.get(0).getValue(), 0.001);
        assertEquals(8.4, values.get(1).getValue(), 0.001);
        assertEquals(2.2, values.get(2).getValue(), 0.001);

        // compare timestamps
        assertEquals(12001, values.get(0).getTimestamp());
        assertEquals(18021, values.get(1).getTimestamp());
        assertEquals(19143, values.get(2).getTimestamp());


        inSeries = createTimeSeriesListWithPartIntersection();
        resultSeries = combiner.combine("mySeries", inSeries);

        // compare values
        values = resultSeries.getValues();
        assertEquals(5, resultSeries.size());
        assertEquals(3, values.get(0).getValue(), 0.001);
        assertEquals(7.2, values.get(1).getValue(), 0.001);
        assertEquals(1.1, values.get(2).getValue(), 0.001);
        assertEquals(4.2, values.get(3).getValue(), 0.001);
        assertEquals(1.1, values.get(4).getValue(), 0.001);

        // compare timestamps
        assertEquals(12000, values.get(0).getTimestamp(), 0.001);
        assertEquals(18000, values.get(1).getTimestamp(), 0.001);
        assertEquals(19000, values.get(2).getTimestamp(), 0.001);
        assertEquals(24000, values.get(3).getTimestamp(), 0.001);
        assertEquals(25000, values.get(4).getTimestamp(), 0.001);
    }

    private List<TimeSeries> createTimeSeriesList() {
        List<TimeSeries> result = new ArrayList<>();

        result.add(createTimeSeries(1, 0));
        result.add(createTimeSeries(2, 600));

        return result;
    }

    private List<TimeSeries> createTimeSeriesListWithPartIntersection() {
        List<TimeSeries> result = new ArrayList<>();

        result.add(createTimeSeries(1, 0));
        result.add(createTimeSeries(2, 6300));

        return result;
    }

    private TimeSeries createTimeSeries(int no, int shift) {

        TimeSeries series = new ClassicTimeSeries("project", "host" + no, "hostGroup" + no,
                "m" + no, "process" + no, "metricGroup" + no, "metric" + no);

        series.addValue(new Value(12001 + shift, 3.0));
        series.addValue(new Value(18021 + shift, 4.2));
        series.addValue(new Value(19143 + shift, 1.1));

        return series;
    }
}
