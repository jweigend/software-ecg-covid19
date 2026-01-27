//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.field;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link Host}.
 */
public class HostTest {

    @Test
    public void testGetAddress() {
        Host host = new Host("HELLO", "ADDRESS");
        String address = host.getAddress();

        assertThat(address, is(equalTo("ADDRESS")));
    }

    @Test
    public void testStaticValueOf() {
        Host host1 = Host.valueOf("HELLO");

        assertThat(host1, instanceOf(Host.class));
    }

    @Test
    public void testCompareTo() {
        Host host1 = new Host("A");
        Host host2 = new Host("B");
        int compared = host1.compareTo(host2);

        assertThat(compared, is(equalTo(-1)));
    }

    @Test
    public void testToString() {
        Host host = new Host("This_is_my_name");
        String hostToString = host.toString();

        assertThat(hostToString, containsString("This_is_my_name"));
    }

}
