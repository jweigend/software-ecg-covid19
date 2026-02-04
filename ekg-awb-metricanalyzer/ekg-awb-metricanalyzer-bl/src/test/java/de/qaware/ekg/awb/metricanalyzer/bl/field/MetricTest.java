//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.field;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link Metric}.
 */
public class MetricTest {

    @Test
    public void testGetName() {
        Metric metric = new Metric("NAME");
        String name = metric.getName();

        assertThat(name, is(equalTo("NAME")));
    }

    /**
     * Tests static initialization.
     */
    @Test
    public void testValueOf() {
        Metric metric = Metric.valueOf("NAME");

        assertThat(metric, instanceOf(Metric.class));
    }

    @Test
    public void testToString() {
        Metric measurement = new Metric("NAME");

        assertThat(measurement.toString(), containsString("NAME"));
    }

    @Test
    public void testCompareTo() {
        Metric metricA = new Metric("A_NAME");
        Metric metricB = new Metric("B_NAME");
        int compare = metricA.compareTo(metricB);

        assertThat(compare, is(equalTo(-1)));
    }

}