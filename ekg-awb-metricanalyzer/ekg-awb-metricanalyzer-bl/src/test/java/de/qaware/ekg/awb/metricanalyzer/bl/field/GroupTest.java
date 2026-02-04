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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link MetricGroup}.
 */
public class GroupTest {

    @Test
    public void testGetGroupName() {
        MetricGroup metricGroup = new MetricGroup("This_is_my_name");
        String groupName = metricGroup.getName();

        assertThat(groupName, is(equalTo("This_is_my_name")));
    }

    @Test
    public void testStaticValueOf() {
        MetricGroup metricGroup1 = MetricGroup.valueOf("HELLO");

        assertThat(metricGroup1, instanceOf(MetricGroup.class));
    }

    @Test
    public void testCompareTo() {
        MetricGroup metricGroup1 = new MetricGroup("A");
        MetricGroup metricGroup2 = new MetricGroup("B");
        int compared = metricGroup1.compareTo(metricGroup2);

        assertThat(compared, is(equalTo(-1)));
    }

    @Test
    public void testToString() {
        MetricGroup metricGroup = new MetricGroup("This_is_my_name");
        String groupToString = metricGroup.toString();

        assertThat(groupToString, containsString("This_is_my_name"));
    }

}
