//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight;

import org.junit.Test;

import static org.junit.Assert.*;

public class LineSegmentTest {

    @Test
    public void testSetCoordinates() {
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(10, 10);

        LineSegment segment = new LineSegment();
        segment.setCoordinates(c1, c2);

        Coordinate testResult1 = segment.getCoordinate(0);
        Coordinate testResult2 = segment.getCoordinate(1);

        assertEquals(c1, testResult1);
        assertEquals(c2, testResult2);
    }

    @Test
    public void testSetCoordinatesCaseBothNull() throws Exception {
        LineSegment segment = new LineSegment();
        Coordinate oldOne1 = segment.getCoordinate(0);
        Coordinate oldOne2 = segment.getCoordinate(1);
        try {
            segment.setCoordinates(null, null);
        } catch (Exception e) {
            //exception was thrown, method works
            //so let's check whether something changed(what should not happen)
            assertEquals(oldOne1, segment.getCoordinate(0));
            assertEquals(oldOne2, segment.getCoordinate(1));
        }
    }

    @Test
    public void testContainsNullCaseTrue() {
        LineSegment segment = new LineSegment();
        assertTrue(segment.containsNull(null, null, null));
    }

    @Test
    public void testContainsNullCaseFalse() {
        LineSegment segment = new LineSegment();
        //all three values are not null -> false
        assertFalse(segment.containsNull("Hello World", 57, new Object()));
    }

    /**
     * Here the getLength() method is tested.
     * Please mention that this is not only a getter-Method,
     * there is happening a real calculation!
     */
    @Test
    public void testGetLength() {
        LineSegment segment = new LineSegment();
        segment.setCoordinates(new Coordinate(0, 0), new Coordinate(3, 4));
        //pythagoras
        double expected = 5.0;
        double actual = segment.getLength();
        assertEquals(expected, actual, 0.000001);
    }

    @Test
    public void testProjectionFactor() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(4, 4));
        double actual = segment.projectionFactor(new Coordinate(2, 2));//assuming 0.5
        assertEquals(0.5, actual, 0.000001);
    }

    @Test
    public void testProjectionFactorCaseZeroLine() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(0, 0));//Line without length
        double actual = segment.projectionFactor(new Coordinate(2, 2));
        assertEquals(Double.POSITIVE_INFINITY, actual, 0.000001);
    }

    @Test
    public void testCompareToCaseNull() {
        LineSegment segment = new LineSegment();
        int x = segment.compareTo(null);
        //x should be bigger than zero, every segment is more than null
        assertTrue(x > 0);
    }

    @Test
    public void testCompareToCaseLower() {
        LineSegment segmentLow = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        LineSegment segmentHigh = new LineSegment(new Coordinate(2, 2), new Coordinate(20, 20));

        int x = segmentLow.compareTo(segmentHigh);
        assertTrue(x < 0);
    }

    @Test
    public void testCompareToCaseEquals() {
        LineSegment segmentLow = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        LineSegment segmentHigh = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));

        int x = segmentLow.compareTo(segmentHigh);
        assertSame("Line segments are equal", x, 0);
    }

    @Test
    public void testCompareToCaseBigger() {
        LineSegment segmentLow = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        LineSegment segmentHigh = new LineSegment(new Coordinate(2, 2), new Coordinate(20, 20));

        int x = segmentHigh.compareTo(segmentLow);
        assertTrue("Line segment is bigger", x > 0);
    }


    /**
     * Testes if the line is horizontal.
     */
    @Test
    public void testLineIsHorizontalCaseFalse() {
        LineSegment notHorizontal = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        assertFalse("line not horizontal", notHorizontal.isHorizontal());
    }

    /**
     * Testes if the line is horizontal.
     */
    @Test
    public void testLineIsHorizontalCaseTrue() {
        LineSegment horizontal = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 0));
        assertTrue("line is horizontal", horizontal.isHorizontal());
    }

    /**
     * Testes if the line is vertical.
     */
    @Test
    public void testLineIsVerticalCaseFalse() {
        LineSegment notVertical = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        assertFalse("line not vertical", notVertical.isVertical());
    }

    /**
     * Testes if the line is vertical.
     */
    @Test
    public void testLineIsVerticalCaseTrue() {
        LineSegment vertical = new LineSegment(new Coordinate(1, 0), new Coordinate(1, 1));
        assertTrue("line is vertical", vertical.isVertical());
    }

    /**
     * Tests if the normalize method works.
     */
    @Test
    public void testNormalizeCaseNoChanges() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(1, 1));
        LineSegment oldSegment = new LineSegment(segment.getCoordinate(0), segment.getCoordinate(1));
        segment.normalize();
        assertEquals("no changes", oldSegment, (segment));
    }

    /**
     * Tests if the normalize method works.
     */
    @Test
    public void testNormalizeCaseSomeChanges() {
        LineSegment segment = new LineSegment(new Coordinate(1, 1), new Coordinate(0, 0));
        LineSegment oldSegment = new LineSegment(segment.getCoordinate(0), segment.getCoordinate(1));
        segment.normalize();
        assertNotEquals("changes have been done", oldSegment, (segment));
    }

    @Test
    public void testClosestPointCaseNull() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(100, 0));
        Coordinate closest = segment.closestPoint(null);

        assertNotEquals("Null input returns null", closest);
    }

    @Test
    public void testClosestPointCaseOnLine() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(100, 0));
        Coordinate coord = new Coordinate(50, 0);
        Coordinate closest = segment.closestPoint(coord);

        assertEquals("point on line returns itself", coord, closest);
    }

    @Test
    public void testClosestPointCaseStartMoreNear() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(100, 0));
        Coordinate coord = new Coordinate(-1, 0);
        Coordinate closest = segment.closestPoint(coord);

        assertEquals("point near start returns start", segment.getCoordinate(0), closest);
    }

    @Test
    public void testClosestPointCaseEndMoreNear() {
        LineSegment segment = new LineSegment(new Coordinate(0, 0), new Coordinate(100, 0));
        Coordinate coord = new Coordinate(101, 0);
        Coordinate closest = segment.closestPoint(coord);

        assertEquals("point near end returns end", segment.getCoordinate(1), closest);
    }


}
