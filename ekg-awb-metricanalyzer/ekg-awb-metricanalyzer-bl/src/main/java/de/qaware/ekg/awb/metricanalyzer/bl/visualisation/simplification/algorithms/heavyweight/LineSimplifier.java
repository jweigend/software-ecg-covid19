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
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight;

import java.util.ArrayList;
import java.util.List;

public final class LineSimplifier {

    private Coordinate[] pts;
    private boolean[] usePt;
    private double distanceTolerance;
    private LineSegment seg = new LineSegment();

    /**
     * Ctor for the LineSimplifier, copies the coordinates array using Coordinate Copy-Ctor.
     *
     * @param pts the Coordinates to simplify
     */
    private LineSimplifier(Coordinate[] pts) {
        this.pts = new Coordinate[pts.length];

        for (int i = 0; i < this.pts.length; i++) {
            this.pts[i] = new Coordinate(pts[i]);
        }
    }

    /**
     * Simplifies an Array of Coordinates with the given distance tolerance.
     * All vertices in the simplified linestring will be within this
     * distance of the original linestring.
     *
     * @param pts coordinates
     * @param distanceTolerance distance tolerance
     * @return an array of Coordinates
     */
    public static Coordinate[] simplify(Coordinate[] pts, double distanceTolerance) {
        if (pts == null || pts.length == 0) {
            throw new IllegalArgumentException("Can't simplify lines without content !");
        }
        LineSimplifier simp = new LineSimplifier(pts);
        simp.setDistanceTolerance(distanceTolerance);

        return simp.simplify();
    }

    /**
     * Sets the distance tolerance for the simplification.
     * All vertices in the simplified linestring will be within this
     * distance of the original linestring.
     *
     * @param distanceTolerance the approximation tolerance to use
     */
    private void setDistanceTolerance(double distanceTolerance) {
        this.distanceTolerance = distanceTolerance;
    }

    /**
     * Simplifies the Sections of Coordinates.(usually LineSegments)
     *
     * @return simplified array of Coordinates
     */
    private Coordinate[] simplify() {
        usePt = new boolean[pts.length];
        for (int i = 0; i < pts.length; i++) {
            usePt[i] = true;
        }
        simplifySection(0, pts.length - 1);
        List<Coordinate> coordList = new ArrayList<>();
        for (int i = 0; i < pts.length; i++) {
            if (usePt[i]) {
                coordList.add(new Coordinate(pts[i]));
            }
        }
        return coordList.toArray(new Coordinate[coordList.size()]);
    }

    /**
     * Simplifies a section.
     * A section of two points can't be simplified.
     *
     * @param i Beginning of section
     * @param j End of section
     */
    private void simplifySection(int i, int j) {
        if ((i + 1) == j) {
            return;
        }

        seg.setP0(pts[i]);
        seg.setP1(pts[j]);

        double maxDistance = -1.0;
        int maxIndex = i;
        for (int k = i + 1; k < j; k++) {
            double distance = seg.distance(pts[k]);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = k;
            }
        }
        if (maxDistance <= distanceTolerance) {
            for (int k = i + 1; k < j; k++) {
                usePt[k] = false;
            }
        } else {
            simplifySection(i, maxIndex);
            simplifySection(maxIndex, j);
        }
    }
}