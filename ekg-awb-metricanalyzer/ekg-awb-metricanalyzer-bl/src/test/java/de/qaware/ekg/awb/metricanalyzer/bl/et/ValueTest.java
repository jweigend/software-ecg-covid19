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
package de.qaware.ekg.awb.metricanalyzer.bl.et;

import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link Value}.
 */
public class ValueTest {

    /**
     * Method to return always the same Date.
     *
     * @return a date for 30.12.2014
     */
    static long makeDate() {
        return 1419894000000L;
    }

    @Test
    public void testImplementingOfComparableValue() {
        Value value = new Value(5, makeDate());
        assertThat(value, is(instanceOf(Comparable.class)));
    }

    @Test
    public void testCopyConstructor() {
        long inputDate = makeDate();
        double inputDouble = 1.337;

        Value firstValue = new Value(inputDate, inputDouble);
        Value secondValue = new Value(firstValue);

        assertThat(secondValue.getTimestamp(), is(equalTo(inputDate)));
        assertThat(secondValue.getValue(), is(equalTo(inputDouble)));
    }

    @Test
    public void testGetValue() {
        Value value = new Value(makeDate(), 1.337);
        assertThat(value.getValue(), is(equalTo(1.337)));
    }

    @Test
    public void testGetDate() {
        long inputDate = makeDate();
        Value value = new Value(inputDate,1.337);
        assertThat(value.getTimestamp(), is(equalTo(inputDate)));
    }

    @Test
    public void testCompareTo() {
        Value valueOne = new Value(makeDate(), 42.0 );

        long laterDate = makeDate() + 1;
        Value valueTwo = new Value(laterDate,42.0);

        assertThat(valueOne.compareTo(valueTwo), is(equalTo(-1)));
    }

}