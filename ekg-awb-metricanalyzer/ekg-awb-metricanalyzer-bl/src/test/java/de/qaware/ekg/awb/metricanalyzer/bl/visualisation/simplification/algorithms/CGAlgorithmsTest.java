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
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms;

import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight.CGAlgorithms;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CGAlgorithmsTest {

    @Test
    public void testSignedAreaCaseNoRing() throws Exception {
        Coordinate[] noRing = {
                new Coordinate(0, 0),
                new Coordinate(2, 2)
        };
        assertEquals("no cycle with 3 nodes possible", 0, CGAlgorithms.signedArea(noRing), 0.00001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignedAreaCaseNull() throws Exception {
        CGAlgorithms.signedArea(null);
    }

    @Test
    public void testSignedArea() throws Exception {
        Coordinate[] square = {
                new Coordinate(0, 0),
                new Coordinate(4, 0),
                new Coordinate(4, 4),
                new Coordinate(0, 4)
        };
        assertEquals("16 areas CCW signed?", -16, CGAlgorithms.signedArea(square), 0.00001);
    }
}
