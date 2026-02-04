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
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight;

/**
 * Specifies and implements various fundamental Computational Geometric algorithms.
 * The algorithms supplied in this class are robust for double-precision floating point.
 *
 * @version 1.7
 */
public final class CGAlgorithms {

    /**
     * A value that indicates an orientation of clockwise, or a right turn.
     */
    public static final int CLOCKWISE = -1;
    public static final int RIGHT = CLOCKWISE;
    /**
     * A value that indicates an orientation of counterclockwise, or a left turn.
     */
    public static final int COUNTERCLOCKWISE = 1;
    public static final int LEFT = COUNTERCLOCKWISE;
    /**
     * A value that indicates an orientation of collinear, or no turn (straight).
     */
    public static final int COLLINEAR = 0;
    public static final int STRAIGHT = COLLINEAR;

    /**
     * Should not be instantiated.
     */
    private CGAlgorithms() {
    }

    /**
     * Computes the distance from a point p to a line segment AB
     * <p>
     * Note: NON-ROBUST!
     *
     * @param p the point to visualisation the distance for
     * @param a one point of the line
     * @param b another point of the line (must be different to A)
     * @return the distance from p to line segment AB
     */
    public static double distancePointLine(Coordinate p, Coordinate a, Coordinate b) {
        // if start==end, then use pt distance
        if (a.equals(b)) {
            return p.distance(a);
        }

        // otherwise use comp.graphics.algorithms Frequently Asked Questions method
    /*(1)     	      AC dot AB
                   r = ---------
                         ||AB||^2
		r has the following meaning:
		r=0 P = A
		r=1 P = B
		r<0 P is on the backward extension of AB
		r>1 P is on the forward extension of AB
		0<r<1 P is interior to AB
	*/

        double r = ((p.getX() - a.getX()) * (b.getX() - a.getX()) + (p.getY() - a.getY()) * (b.getY() - a.getY()))
                /
                ((b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY()));

        if (r <= 0.0) {
            return p.distance(a);
        }
        if (r >= 1.0) {
            return p.distance(b);
        }


    /*(2)
             (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		s = -----------------------------
		             	L^2

		Then the distance from C to P = |s|*L.
	*/

        double s = ((a.getY() - p.getY()) * (b.getX() - a.getX()) - (a.getX() - p.getX()) * (b.getY() - a.getY()))
                /
                ((b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY()));

        return
                Math.abs(s) *
                        Math.sqrt((b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY()));
    }

    /**
     * Computes the signed area for a ring.
     * The signed area is positive if
     * the ring is oriented CW, negative if the ring is oriented CCW,
     * and zero if the ring is degenerate or flat.
     *
     * @param ring the coordinates forming the ring
     * @return the signed area of the ring
     */
    public static double signedArea(Coordinate[] ring) {
        if (ring == null) {
            throw new IllegalArgumentException("The ring mustn't be null!");
        }
        if (ring.length < 3) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < ring.length - 1; i++) {
            double bx = ring[i].getX();
            double by = ring[i].getY();
            double cx = ring[i + 1].getX();
            double cy = ring[i + 1].getY();
            sum += (bx + cx) * (cy - by);
        }
        return -sum / 2.0;
    }


}
