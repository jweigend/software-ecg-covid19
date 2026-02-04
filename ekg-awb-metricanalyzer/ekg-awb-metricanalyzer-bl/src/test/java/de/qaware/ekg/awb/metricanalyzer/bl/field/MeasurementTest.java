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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link Measurement}.
 */
public class MeasurementTest {

    /**
     * Method to return always the same Date.
     *
     * @return a date for 30.12.2014
     */
    private long makeStart() {
        return 1419894000000L;
    }

    /**
     * Method to return always the same Date.
     *
     * @return a date for 31.12.2014
     */
    private long makeEnd() {
        return 1419980400000L;
    }

    @Test
    public void testGetName() {
        Measurement measurement = new Measurement("NAME", makeStart(), makeEnd());
        String name = measurement.getName();

        assertThat(name, is(equalTo("NAME")));
    }

    @Test
    public void testGetStart() {
        Measurement measurement = new Measurement("NAME", makeStart(), makeEnd());
        long start = measurement.getStart();

        assertThat(start, is(equalTo(makeStart())));
    }

    @Test
    public void testGetEnd() {
        Measurement measurement = new Measurement("NAME", makeStart(), makeEnd());
        long end = measurement.getEnd();

        assertThat(end, is(equalTo(makeEnd())));
    }

    @Test
    public void testToString() {
        Measurement measurement = new Measurement("NAME", makeStart(), makeEnd());

        assertThat(measurement.toString(), containsString("NAME"));
    }

    @Test
    public void testCompareTo() {
        Measurement measurementA = new Measurement("A", makeStart(), makeEnd());
        Measurement measurementB = new Measurement("B", makeStart(), makeEnd());
        int compared = measurementA.compareTo(measurementB);

        assertThat(compared, is(equalTo(-1)));
    }

}