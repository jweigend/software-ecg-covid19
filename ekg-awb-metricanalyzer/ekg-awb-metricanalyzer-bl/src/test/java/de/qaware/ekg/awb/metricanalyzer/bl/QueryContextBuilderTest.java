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
package de.qaware.ekg.awb.metricanalyzer.bl;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link QueryFilterParams.Builder}.
 */
public class QueryContextBuilderTest {

    @Test
    public void testWithProcess() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withProcess(new Process("abc", "")).build();
        assertThat(query.getProcessName(), is(equalTo("abc")));
    }

    @Test
    public void testWithRawQuery() throws Exception {
        QueryFilterParams query = new QueryFilterParams.Builder().withRawQuery("a Raw Query").build();
        assertThat(query.getRawQuery(), is(equalTo("a Raw Query")));
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
