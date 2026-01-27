/**
 * ______________________________________________________________________________
 * <p>
 * Project: Software EKG
 * ______________________________________________________________________________
 * <p>
 * created by: f.lautenschlager
 * creation date: 12.02.14 12:35
 * description:
 * ______________________________________________________________________________
 * <p>
 * Copyright: (c) QAware GmbH, all rights reserved
 * ______________________________________________________________________________
 */
package de.qaware.ekg.awb.metricanalyzer.bl.et;


import de.qaware.ekg.awb.sdk.datamodel.Counter;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * Unit test for the {@link TimeSeries}.
 */
public class TimeSeriesTest {

    private TimeSeries testSeries;
    public static final Instant FIRST_DATE = Instant.now();
    public static final Instant SECOND_DATE = FIRST_DATE.plusSeconds(5);
    public static final Instant THIRD = FIRST_DATE.plusSeconds(50);

    @Before
    public void beforeEachTest() {
        testSeries = new TimeSeries("TestProject", "TestCluster", "TestHost", "TestNamespace", "TestService",
                "TestPod", "TestContainer", "TestMeasurement",
                "TestProcess", "TestGroup", "MyClass.callAMethod");
    }

    @Test
    public void testAddPoint() throws Exception {
        testSeries.addValue(new Value(4711, Instant.now().toEpochMilli()));
        assertThat(testSeries.size(), is(1l));
    }

    @Test
    public void testAddPointNull() throws Exception {
        testSeries.addValue(null);
        assertThat(testSeries.size(), is(0l));
    }

    @Test
    public void testGetStartDateNull() {
        assertThat(testSeries.getStartDate(), equalTo(0l));
    }

    @Test
    public void testGetEndDateNull() {
        assertThat(testSeries.getEndDate(), equalTo(0l));
    }

    @Test
    public void testGetStartDate() throws Exception {
        testSeries.addValue(new Value(4811, SECOND_DATE.toEpochMilli()));
        testSeries.addValue(new Value(4711, FIRST_DATE.toEpochMilli()));

        assertThat(testSeries.getStartDate(), is(4711L));
    }

    @Test
    public void testGetEndDate() throws Exception {
        testSeries.addValue(new Value(4811, SECOND_DATE.toEpochMilli()));
        testSeries.addValue(new Value(4711, FIRST_DATE.toEpochMilli()));
        testSeries.addValue(new Value(7911, THIRD.toEpochMilli()));

        assertThat(testSeries.getEndDate(), is(7911L));
    }

    @Test
    public void testClearSeries() throws Exception {
        testSeries.addValue(new Value(4711, Instant.now().toEpochMilli()));
        assertThat(testSeries.size(), is(1l));
        testSeries.clear();
        assertThat(testSeries.size(), is(0l));
        assertThat(testSeries.getStartDate(), is(-1l));
        assertThat(testSeries.getEndDate(), is(-1l));
    }

    @Test
    public void testAddAllPoints() throws Exception {
        List<Value> points = new ArrayList<Value>() {
            {
                add(new Value(4711, Instant.now().toEpochMilli()));
                add(new Value(4712, Instant.now().toEpochMilli()));
            }
        };
        assertThat(testSeries.empty(), is(true));
        testSeries.addAll(points);
        assertThat(testSeries.size(), is(2l));
    }

    @Test
    public void testToCounter() throws Exception {
        List<Value> points = new ArrayList<>() {
            {
                add(new Value(4711, Instant.now().toEpochMilli()));
                add(new Value(4712, Instant.now().toEpochMilli()));
            }
        };
        testSeries.addAll(points);
        Counter actual = testSeries.toCounter();
        assertThat(actual.getCounterName(), is(equalTo("MyClass.callAMethod[TestProcess,TestHost]")));
        assertThat(actual.getValues(), hasSize(2));
    }

}
