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

import java.io.Serializable;

/**
 * Represents a line segment defined by two {@link Coordinate}s.
 * Provides methods to visualisation various geometric properties
 * and relationships of line segments.
 * <p>
 * This class is designed to be easily mutable (to the extent of
 * having its contained points public).
 * This supports a common pattern of reusing a single LineSegment
 * object as a way of computing segment properties on the
 * segments defined by arrays or lists of {@link Coordinate}s.
 * @version 1.7
 */
public class LineSegment implements Comparable<LineSegment>, Serializable {
    private static final long serialVersionUID = 3252005833466256227L;

    private Coordinate p0;
    private Coordinate p1;


    /**
     * Creates a LineSegment from the two given Coordinates.
     *
     * @param p0 first Coordinate
     * @param p1 second Coordinate
     */
    public LineSegment(Coordinate p0, Coordinate p1) {
        this.p0 = p0;
        this.p1 = p1;
    }


    /**
     * Creates a LineSegment with empty Coordinates.
     */
    public LineSegment() {
        this(new Coordinate(), new Coordinate());
    }

    /**
     * Getter for the Coordinates. Decide which Coordinate will be returned bei de given index.
     *
     * @param index the index of the Coordinate to select.
     * @return the selected Coordinate
     */
    public Coordinate getCoordinate(int index) {
        if (index == 0) {
            return p0;
        }
        return p1;
    }

    /**
     * Setter for the Coordinates.
     * If one of the arguments is null, a IllegalArgumentException
     * is thrown.
     *
     * @param p0 the first Coordinate to get the properties from
     * @param p1 the second Coordinate to get the properties from
     */
    public void setCoordinates(Coordinate p0, Coordinate p1) {
        if (containsNull(p0, p1)) {
            throw new IllegalArgumentException("You mustn't pass null Coordinates to a LineSegment!");
        }
        this.p0.setX(p0.getX());
        this.p0.setY(p0.getY());
        this.p1.setX(p1.getX());
        this.p1.setY(p1.getY());
    }

    /**
     * Checks whether one of the input values is null.
     *
     * @return true, if one or more input values
     * are null.
     */
    boolean containsNull(Object... inputs) {
        for (Object o : inputs) {
            if (o == null) {
                return true;
            }
        }
        //No null value found
        return false;
    }

    /**
     * Computes the length of the line segment.
     *
     * @return the length of the line segment
     */
    public double getLength() {
        return p0.distance(p1);
    }

    /**
     * Tests whether the segment is horizontal.
     *
     * @return <code>true</code> if the segment is horizontal
     */
    public boolean isHorizontal() {
        return p0.getY() == p1.getY();
    }

    /**
     * Tests whether the segment is vertical.
     *
     * @return <code>true</code> if the segment is vertical
     */
    public boolean isVertical() {
        return p0.getX() == p1.getX();
    }


    /**
     * Reverses the direction of the line segment.
     */
    public void reverse() {
        Coordinate temp = p0;
        p0 = p1;
        p1 = temp;
    }

    /**
     * Puts the line segment into a normalized form.
     * This is useful for using line segments in maps and indexes when
     * topological equality rather than exact equality is desired.
     * A segment in normalized form has the first point smaller
     * than the second (according to the standard ordering on {@link Coordinate}).
     */
    public void normalize() {
        if (p1.compareTo(p0) < 0) {
            reverse();
        }
    }


    /**
     * Computes the distance between this line segment and a given point.
     *
     * @param p point
     * @return the distance from this segment to the given point
     */
    public double distance(Coordinate p) {
        return CGAlgorithms.distancePointLine(p, p0, p1);
    }


    /**
     * Computes the Projection Factor for the projection of the point p
     * onto this LineSegment.  The Projection Factor is the constant r
     * by which the vector for this segment must be multiplied to
     * equal the vector for the projection of <tt>p<//t> on the line
     * defined by this segment.
     * <p>
     * The projection factor returned will be in the range <tt>(-inf, +inf)</tt>.
     *
     * @param p the point to visualisation the factor for
     * @return the projection factor for the point
     */
    public double projectionFactor(Coordinate p) {
        if (p.equals(p0)) {
            return 0.0;
        }
        if (p.equals(p1)) {
            return 1.0;
        }
        //If out line has no length we can't calculate it
        if (Math.abs(getLength() - 0.0) < 0.00001) {
            //positive infinity because when we had a value a little bit
            //more than 0 we would need almost this value
            //and the function is running against positive infinity
            //at the x-value 0
            return Double.POSITIVE_INFINITY;
        }

        // Otherwise, use comp.graphics.algorithms Frequently Asked Questions method
    /*     	      AC dot AB
                   r = ---------
                         ||AB||^2
                r has the following meaning:
                r=0 P = A
                r=1 P = B
                r<0 P is on the backward extension of AB
                r>1 P is on the forward extension of AB
                0<r<1 P is interior to AB
        */
        double dx = p1.getX() - p0.getX();
        double dy = p1.getY() - p0.getY();
        double len2 = dx * dx + dy * dy;
        return ((p.getX() - p0.getX()) * dx + (p.getY() - p0.getY()) * dy)
                / len2;
    }

    /**
     * Compute the projection of a point onto the line determined
     * by this line segment.
     * <p>
     * Note that the projected point
     * may lie outside the line segment.  If this is the case,
     * the projection factor will lie outside the range [0.0, 1.0].
     *
     * @param p Coordinate to project
     * @return the projected Coordinate
     */
    public Coordinate project(Coordinate p) {
        if (p.equals(p0) || p.equals(p1)) {
            return new Coordinate(p);
        }

        double r = projectionFactor(p);
        Coordinate coord = new Coordinate();
        coord.setX(p0.getX() + r * (p1.getX() - p0.getX()));
        coord.setY(p0.getY() + r * (p1.getY() - p0.getY()));
        return coord;
    }

    /**
     * Project a line segment onto this line segment and return the resulting
     * line segment.  The returned line segment will be a subset of
     * the target line line segment.  This subset may be null, if
     * the segments are oriented in such a way that there is no projection.
     * <p>
     * Note that the returned line may have zero length (i.e. the same endpoints).
     * This can happen for instance if the lines are perpendicular to one another.
     *
     * @param seg the line segment to project
     * @return the projected line segment, or <code>null</code> if there is no overlap
     */
    public LineSegment project(LineSegment seg) {
        double pf0 = projectionFactor(seg.p0);
        double pf1 = projectionFactor(seg.p1);
        // check if segment projects at all
        if (pf0 >= 1.0 && pf1 >= 1.0) {
            return null;
        }
        if (pf0 <= 0.0 && pf1 <= 0.0) {
            return null;
        }

        Coordinate newp0 = project(seg.p0);
        if (pf0 < 0.0) {
            newp0 = p0;
        }
        if (pf0 > 1.0) {
            newp0 = p1;
        }

        Coordinate newp1 = project(seg.p1);
        if (pf1 < 0.0) {
            newp1 = p0;
        }
        if (pf1 > 1.0) {
            newp1 = p1;
        }

        return new LineSegment(newp0, newp1);
    }

    /**
     * Computes the closest point on this line segment to another point.
     *
     * @param p the point to find the closest point to
     * @return a Coordinate which is the closest point on the line segment to the point p
     */
    public Coordinate closestPoint(Coordinate p) {
        if (p == null) {
            return null;//It is not possible to measure the distance between
            //a line and null!
        }
        double factor = projectionFactor(p);
        if (factor > 0 && factor < 1) {
            return project(p);
        }
        double dist0 = p0.distance(p);
        double dist1 = p1.distance(p);
        if (dist0 < dist1) {
            return p0;
        }
        return p1;
    }


    /**
     * Returns <code>true</code> if <code>other</code> has the same values for
     * its points.
     *
     * @param o other object
     * @return <code>true</code> if <code>other</code> is a <code>LineSegment</code>
     * with the same values for the x and y ordinates.
     */
    public boolean equals(Object o) {
        if (!(o instanceof LineSegment)) {
            return false;
        }
        LineSegment other = (LineSegment) o;
        return p0.equals(other.p0) && p1.equals(other.p1);
    }

    /**
     * Gets a hashcode for this object.
     *
     * @return a hashcode for this object
     */
    public int hashCode() {
        long bits0 = java.lang.Double.doubleToLongBits(p0.getX());
        bits0 ^= java.lang.Double.doubleToLongBits(p0.getY()) * 31;
        int hash0 = (((int) bits0) ^ ((int) (bits0 >> 32)));

        long bits1 = java.lang.Double.doubleToLongBits(p1.getX());
        bits1 ^= java.lang.Double.doubleToLongBits(p1.getY()) * 31;
        int hash1 = (((int) bits1) ^ ((int) (bits1 >> 32)));

        // XOR is supposed to be a good way to addValues hashcodes
        return hash0 ^ hash1;
    }

    /**
     * Compares this object with the specified object for order.
     * Uses the standard lexicographic ordering for the points in the LineSegment.
     *
     * @param other the <code>LineSegment</code> with which this <code>LineSegment</code>
     *              is being compared
     * @return a negative integer, zero, or a positive integer as this <code>LineSegment</code>
     * is less than, equal to, or greater than the specified <code>LineSegment</code>
     */
    @Override
    public int compareTo(LineSegment other) {
        if (other == null) {
            return 1;//every lineSegment is bigger than null
        }
        int comp0 = p0.compareTo(other.p0);
        if (comp0 != 0) {
            return comp0;
        }
        return p1.compareTo(other.p1);
    }

    @Override
    public String toString() {
        return "LINESTRING( " +
                p0.getX() + " " + p0.getY()
                + ", " +
                p1.getX() + " " + p1.getY() + ")";
    }

    /**
     * Setter for property p0.
     *
     * @param p0 Value to set for property p0.
     */
    public void setP0(Coordinate p0) {
        this.p0 = p0;
    }

    /**
     * Setter for property p1.
     *
     * @param p1 Value to set for property p1.
     */
    public void setP1(Coordinate p1) {
        this.p1 = p1;
    }
}
