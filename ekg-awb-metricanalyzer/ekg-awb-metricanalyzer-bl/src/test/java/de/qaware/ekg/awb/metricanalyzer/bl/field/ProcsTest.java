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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link Process}.
 */
public class ProcsTest {

    @Test
    public void testValueOf() {
        Process process = Process.valueOf("I_AM_A_PROCESS");

        assertThat(process, instanceOf(Process.class));
    }

    @Test
    public void testGetName() {
        Process process = new Process("I_AM_A_PROCESS", "MY_DESCRIPTION");
        String name = process.getName();

        assertThat(name, is(equalTo("I_AM_A_PROCESS")));
    }

    @Test
    public void testGetDescription() {
        Process process = new Process("I_AM_A_PROCESS", "MY_DESCRIPTION");
        String desc = process.getDescription();

        assertThat(desc, is(equalTo("MY_DESCRIPTION")));
    }

    @Test
    public void testToString() {
        Process process = new Process("I_AM_A_PROCESS", "DESC");

        assertThat(process.toString(), containsString("I_AM_A_PROCESS"));
    }

    @Test
    public void testCompareTo() {
        Process procA = new Process("A_PROCESS", "DESC");
        Process procB = new Process("B_PROCESS", "DESC");
        int compare = procA.compareTo(procB);

        assertThat(compare, is(equalTo(-1)));
    }

}