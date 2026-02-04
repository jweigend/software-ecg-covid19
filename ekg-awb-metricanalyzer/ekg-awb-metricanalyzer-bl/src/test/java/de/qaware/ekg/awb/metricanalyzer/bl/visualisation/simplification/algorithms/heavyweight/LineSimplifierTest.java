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

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * This test is written for the LineSimplifier of
 * the Software EKG.
 */
public class LineSimplifierTest {

    @Test
    public void testSimplify() throws Exception {
        double distance = 10.0;
        Coordinate[] coordinates = {
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 2),
                new Coordinate(3, 3),
                new Coordinate(4, 4)
        };//points of one line
        System.err.println(Arrays.toString(coordinates));

        Coordinate[] simplified = LineSimplifier.simplify(coordinates, distance);
        assertSame(simplified.length, 2);//One line can always be represented by two points
        assertTrue(simplified[0].equals(new Coordinate(0, 0)));//staring point stays the same
        assertTrue(simplified[1].equals(new Coordinate(4, 4)));//end point stays the same
    }

    @Test
    public void testSimplifyCaseNull() throws Exception {
        double distance = 10.0;
        Coordinate[] coords = null;

        Coordinate[] simplified = null;
        try {
            simplified = LineSimplifier.simplify(coords, distance);
        } catch (IllegalArgumentException e) {
            assertNull(simplified);
        }
    }

    @Test
    public void testSimplifyCaseSingleSection() throws Exception {
        Coordinate[] coordinates = {
                new Coordinate(0, 0),
                new Coordinate(1, 1)
        };//two points on a line

        Coordinate[] simplified = LineSimplifier.simplify(coordinates, 10.0);
        assertArrayEquals("No changes made in 2 points-line", coordinates, simplified);
    }


}
