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
package de.qaware.ekg.awb.metricanalyzer.bl.bookmark;

import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.MetricQueryBookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link MetricQueryBookmark.Builder}.
 */
public class MetricQueryBookmarkBuilderTest {
    @Test
    public void testBuildFormMetricContext() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder()
                .withMetric(new Metric("abc"))
                .withExclude(new Metric("exclude"))
                .withExpertMode(true)
                .withMultiMetricMode(true)
                .build();

        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder(query)
                .withName("Bookmark Name")
                .build();

        assertThat(bookmark.getName(), is(equalTo("Bookmark Name")));
        assertThat(bookmark.getExclude(), is(equalTo(query.getExcludeMetricName())));
        assertThat(bookmark.getMetric(), is(equalTo((query.getMetricName()))));
        assertThat(bookmark.isMultiMetricMode(), is(query.isMultiMetricMode()));
        assertThat(bookmark.isExpertMode(), is(query.isExpertMode()));
        assertThat(bookmark.getProject(), is(equalTo(query.getProjectName())));
        assertThat(bookmark.getHost(), is(equalTo(query.getHostName())));
        assertThat(bookmark.getGroup(), is(equalTo(query.getMetricGroupName())));
        assertThat(bookmark.getMeasurement(), is(equalTo(query.getMeasurementName())));
        assertThat(bookmark.getProcess(), is(equalTo(query.getProcessName())));
        assertThat(bookmark.getStart(), is(equalTo(query.getStart())));
        assertThat(bookmark.getStop(), is(equalTo(query.getEnd())));
    }

    @Test
    public void testWithExclude() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withExclude("exclude").build();
        assertThat(bookmark.getExclude(), is(equalTo("exclude")));
    }

    @Test
    public void testWithMetric() throws Exception {
        MetricQueryBookmark.Builder builder = new MetricQueryBookmark.Builder().withMetric("metric");
        assertThat(builder.build().getMetric(), is(equalTo("metric")));
        assertThat(builder.clearMetrics().withMetric("abc").build().getMetric(), is(equalTo("abc")));
    }

    @Test
    public void testWithRegex() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withRegex(true).build();
        assertThat(bookmark.isRegex(), is(true));
    }

    @Test
    public void testWithMultiMetricMode() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withMultiMetricMode(true).build();
        assertThat(bookmark.isMultiMetricMode(), is(true));
    }

    @Test
    public void testWithExpertMode() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withExpertMode(true).build();
        assertThat(bookmark.isExpertMode(), is(true));
    }

    @Test
    public void testWithSeries() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withProject("series").build();
        assertThat(bookmark.getProject(), is(equalTo("series")));
    }

    @Test
    public void testWithHost() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withHost("host").build();
        assertThat(bookmark.getHost(), is(equalTo("host")));
    }

    @Test
    public void testWithGroup() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withGroup("group").build();
        assertThat(bookmark.getGroup(), is(equalTo("group")));
    }

    @Test
    public void testWithMeasurement() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withMeasurement("m").build();
        assertThat(bookmark.getMeasurement(), is(equalTo("m")));
    }

    @Test
    public void testWithProcess() throws Exception {
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withProcess("m").build();
        assertThat(bookmark.getProcess(), is(equalTo("m")));
    }

    @Test
    public void testWithStart() throws Exception {
        long now = Instant.now().toEpochMilli();
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withStart(now).build();
        assertThat(bookmark.getStart(), is(equalTo(now)));
    }

    @Test
    public void testWithStop() throws Exception {
        long now = Instant.now().toEpochMilli();
        MetricQueryBookmark bookmark = new MetricQueryBookmark.Builder().withStop(now).build();
        assertThat(bookmark.getStop(), is(equalTo(now)));
    }

    @Test
    public void testBuild() throws Exception {
        Bookmark.Builder<MetricQueryBookmark> builder = new MetricQueryBookmark.Builder().withName("name");
        MetricQueryBookmark bookmark1 = builder.build();
        MetricQueryBookmark bookmark2 = builder.build();

        assertThat(bookmark1.getName(), is(equalTo("name")));
        assertThat(bookmark1, is(notNullValue()));
        assertThat(bookmark2, is(notNullValue()));
        assertThat(bookmark1, is(equalTo(bookmark1)));
        assertThat(bookmark1, is(equalTo(bookmark2)));
        assertThat(bookmark1, is(not(equalTo(null))));
        assertThat(bookmark1, is(not(equalTo(""))));
        assertThat(bookmark1, is(not(sameInstance(bookmark2))));
    }
}
