//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.query;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.project.api.model.Project;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class MetricQueryParamsTest {

    @Mock
    private QueryFilterParams mock;
    @Mock
    private Metric metric;
    @Mock
    private Metric exclude;
    private Set<Metric> metrics = new ListOrderedSet<>();
    @Mock
    private Project project;
    @Mock
    private Host host;
    @Mock
    private Measurement measurement;
    @Mock
    private Process process;
    @Mock
    private MetricGroup metricGroup;
    private long start = 0;
    private long stop = 1;

    private QueryFilterParams metricContext;

    @Before
    public void setUp() throws Exception {
        mock = new QueryFilterParams.Builder()
                .withProject(project)
                .withHost(host)
                .withMeasurement(measurement)
                .withProcess(process)
                .withMetricGroup(metricGroup)
                .withStart(start)
                .withStop(stop)
                .build();
        metric = new Metric("A existing name");
        exclude = new Metric("Exclude");
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric(metric)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(false)
                .build();
    }

    @Test
    public void testGetExcludeMetricNameCaseNormal() throws Exception {
        assertThat(metricContext.getExcludeMetricName(), is(equalTo(exclude.getName())));
    }

    @Test
    public void testConstructorCaseMetricNull() throws Exception {
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric((Metric) null)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(false)
                .build();
        //ATTENZIONE! DEFAULT SIGN IS STAR
        assertThat(metricContext.getMetric().getName(), is(equalTo("*")));
    }

    @Test
    public void testConstructorCaseExcludeNull() throws Exception {
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric(metric)
                .withExclude((Metric) null)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(false)
                .build();
        //ATTENZIONE! DEFAULT SIGN IS EMPTY
        assertThat(metricContext.getExcludeMetric().getName(), is(equalTo("")));
    }

    @Test
    public void testGetExcludeMetric() throws Exception {
        assertThat(metricContext.getExcludeMetric(), is(equalTo(exclude)));
    }

    @Test
    public void testIsExpertModeCaseTrue() throws Exception {
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric(metric)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(true)
                .build();
        assertTrue(metricContext.isExpertMode());
    }

    @Test
    public void testIsExpertModeCaseFalse() throws Exception {
        //false by default
        assertFalse(metricContext.isExpertMode());
    }

    @Test
    public void testIsMultiMetricModeCaseTrue() throws Exception {
        metrics.add(new Metric("abc"));
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric(metric)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(true)
                .withExpertMode(false)
                .build();
        assertTrue(metricContext.isMultiMetricMode());
    }

    @Test
    public void testIsMultiMetricModeCaseFalse() throws Exception {
        //false by default
        assertFalse(metricContext.isMultiMetricMode());
    }

    @Test
    public void testGetMetricNameCaseNormal() throws Exception {
        assertThat(metricContext.getMetricName(), is(equalTo(metric.getName())));
    }

    @Test
    public void testGetMetricNameCaseNull() throws Exception {
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric((Metric) null)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(false)
                .build();

        //expect star sign
        String metricName = metricContext.getMetricName();
        assertThat(metricName, is(equalTo("*")));
    }

    @Test
    public void testGetMetricNamesCaseEmpty() throws Exception {
        List<String> emptyList = new ArrayList<>();
        assertThat(metricContext.getMetricNames(), is(equalTo(emptyList)));
    }

    @Test
    public void testGetMetricNamesCaseFilled() throws Exception {
        metrics = new ListOrderedSet<>();
        metrics.add(new Metric("first"));
        metrics.add(new Metric("second"));
        metricContext = new QueryFilterParams.Builder(mock)
                .withMetric(metric)
                .withExclude(exclude)
                .withMetrics(metrics)
                .withMultiMetricMode(false)
                .withExpertMode(false)
                .build();
        List<String> expected = metrics.stream().map(Metric::getName).collect(Collectors.toList());

        List<String> notEmpty = metricContext.getMetricNames();

        assertThat(notEmpty, is(equalTo(expected)));
    }

    @Test
    public void testGetMetric() throws Exception {
        assertThat(metricContext.getMetric(), is(equalTo(metric)));
    }

    @Test
    public void testGetMetrics() throws Exception {
        assertThat(metricContext.getMetrics(), is(equalTo(metrics)));
    }

    @Test
    public void testGetLimit() throws Exception {
        //500_000 is the maximum allowed number of rows of a solr query
        assertThat(metricContext.getLimit(), is(equalTo(500_000)));
    }

}
