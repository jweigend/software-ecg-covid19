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
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification;

import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight.Coordinate;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CoordinateTest {

    /**
     * Constant for indicating that this number does not matter.
     */
    private static final int ANY_NUMBER = -1337;

    @Test
    public void testDistance() {
        Coordinate implicitOrigin = new Coordinate();
        Coordinate otherCoordinate = new Coordinate(4, 3);

        double distanceBetween = implicitOrigin.distance(otherCoordinate);

        assertThat(5.0, is(equalTo(distanceBetween)));
    }

    @Test
    public void testCompareToWithSmallerX() {
        Coordinate first = new Coordinate(0, ANY_NUMBER);
        Coordinate otherCoordinate = new Coordinate(1, ANY_NUMBER);

        int compare = first.compareTo(otherCoordinate);

        assertThat(-1, is(equalTo(compare)));
    }

    @Test
    public void testCompareToWithBiggerX() {
        Coordinate first = new Coordinate(1, ANY_NUMBER);
        Coordinate otherCoordinate = new Coordinate(0, ANY_NUMBER);

        int compare = first.compareTo(otherCoordinate);

        assertThat(1, is(equalTo(compare)));
    }

    @Test
    public void testCompareToWithSameXSmallerY() {
        Coordinate first = new Coordinate(0, 0);
        Coordinate otherCoordinate = new Coordinate(0, 1);

        int compare = first.compareTo(otherCoordinate);

        assertThat(-1, is(equalTo(compare)));
    }

    @Test
    public void testCompareToWithSameXBiggerY() {
        Coordinate first = new Coordinate(0, 1);
        Coordinate otherCoordinate = new Coordinate(0, 0);

        int compare = first.compareTo(otherCoordinate);

        assertThat(1, is(equalTo(compare)));
    }

    @Test
    public void testCompareToAreEqual() {
        Coordinate implicitOrigin = new Coordinate();
        Coordinate explicitOrigin = new Coordinate(0, 0);

        int compare = implicitOrigin.compareTo(explicitOrigin);

        assertThat(0, is(equalTo(compare)));
    }

    @Test
    public void testEqualsCaseNull() {
        Coordinate origin = new Coordinate();
        assertNotEquals("No coordinate is null", origin, null);
    }

    @Test
    public void testEqualsCaseAlias() {
        Coordinate origin = new Coordinate();
        assertEquals("Aliases refer to same object", origin, origin);
    }

    @Test
    public void testEqualsCaseOtherClass() {
        Coordinate origin = new Coordinate();
        Object other = new Object();
        assertNotEquals("Different classes produce different objects", origin, other);
    }
}