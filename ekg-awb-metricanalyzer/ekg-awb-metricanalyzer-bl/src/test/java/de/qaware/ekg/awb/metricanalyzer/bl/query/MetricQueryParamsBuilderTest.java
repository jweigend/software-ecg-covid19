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
import org.junit.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link QueryFilterParams.Builder}.
 */
public class MetricQueryParamsBuilderTest {
    public static final long START = Instant.now().minusSeconds(120).toEpochMilli();
    public static final long END = Instant.now().toEpochMilli();

    @Test
    public void testCopy() throws Exception {
        Set<Metric> metrics = new HashSet<>();
        metrics.add(new Metric("abc"));
        metrics.add(new Metric("abc1"));

        QueryFilterParams query = new QueryFilterParams.Builder()
                .withMetric(new Metric("abc"))
                .withExclude(new Metric("def"))
                .withMetrics(metrics)
                .withExpertMode(true)
                .withMultiMetricMode(true)
                .withProject(new Project("abc"))
                .withHost(new Host("abc"))
                .withMetricGroup(new MetricGroup("abc"))
                .withMeasurement(new Measurement("abc", -1, -1))
                .withProcess(new Process("abc", ""))
                .withStart(START)
                .withStop(END)
                .build();

        QueryFilterParams query1 = new QueryFilterParams.Builder(query)
                .withMetric(new Metric("def"))
                .withExclude((Metric) null)
                .build();

        assertThat(query1.getMetricName(), is(equalTo("def")));
        assertThat(query1.getExcludeMetricName(), is(equalTo("")));

        assertThat(query1.getMetrics(), is(equalTo(query.getMetrics())));
        assertThat(query1.isExpertMode(), is(equalTo(query.isExpertMode())));
        assertThat(query1.isMultiMetricMode(), is(equalTo(query.isMultiMetricMode())));

        assertThat(query1.getProjectName(), is(equalTo(query.getProjectName())));
        assertThat(query1.getHostName(), is(equalTo(query.getHostName())));
        assertThat(query1.getMetricGroupName(), is(equalTo(query.getMetricGroupName())));
        assertThat(query1.getMeasurementName(), is(equalTo(query.getMeasurementName())));
        assertThat(query1.getProcessName(), is(equalTo(query.getProcessName())));
        assertThat(query1.getStart(), is(equalTo(query.getStart())));
        assertThat(query1.getEnd(), is(equalTo(query.getEnd())));
    }

    @Test
    public void testWithMetricsNull() throws Exception {
        QueryFilterParams context = new QueryFilterParams.Builder().withMetrics(null).build();
        assertThat(context.getMetrics(), hasSize(0));
    }

    @Test
    public void testWithMetric() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withMetric(new Metric("abc")).build();
        assertThat(query.getMetricName(), is(equalTo("abc")));
    }

    @Test
    public void testWithMetrics() throws Exception {
        Set<Metric> metrics = new HashSet<>();
        metrics.add(new Metric("abc"));
        metrics.add(new Metric("abc1"));
        QueryFilterParams query = new QueryFilterParams.Builder().withMultiMetricMode(true).withMetrics(metrics).build();
        assertThat(query.isMultiMetricMode(), is(true));
        assertThat(query.getMetrics(), hasSize(2));
    }

    @Test
    public void testWithExclude() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withExclude(new Metric("abc")).build();
        assertThat(query.getExcludeMetricName(), is(equalTo("abc")));
    }

    @Test
    public void testWithIsExpertMode() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withExpertMode(true).build();
        assertThat(query.isExpertMode(), is(true));
    }

    @Test
    public void testWithIsMultiMetricMode() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withMultiMetricMode(true).build();
        assertThat(query.isMultiMetricMode(), is(false));
    }

    @Test
    public void testBuild() throws Exception {
        QueryFilterParams.Builder builder = new QueryFilterParams.Builder().withStop(Instant.now().toEpochMilli());
        QueryFilterParams build1 = builder.build();
        QueryFilterParams build2 = builder.build();
        assertThat(build1, is(notNullValue()));
        assertThat(build2, is(notNullValue()));
        assertThat(build2, is(equalTo(build1)));
        assertThat(build2, is(not(sameInstance(build1))));
    }
}
