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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents a Point.
 */
public class Coordinate implements Comparable<Coordinate>, Serializable {

    private static final long serialVersionUID = -1759988281129801829L;

    private static final double ORIGIN = 0.0;

    /**
     * The x-coordinate.
     */
    private double x;

    /**
     * The y-coordinate.
     */
    private double y;

    /**
     * Constructs a <code>Coordinate</code> at (x,y).
     *
     * @param x the x-value
     * @param y the y-value
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a <code>Coordinate</code> at (0,0).
     */
    public Coordinate() {
        this(ORIGIN, ORIGIN);
    }

    /**
     * Constructs a <code>Coordinate</code> having the same (x,y) values as
     * <code>other</code>.
     *
     * @param c the <code>Coordinate</code> to copy.
     */
    public Coordinate(Coordinate c) {
        this(c.x, c.y);
    }

    /**
     * Computes the 2-dimensional Euclidean distance to another location.
     *
     * @param p a point
     * @return the 2-dimensional Euclidean distance between the locations
     */
    public double distance(Coordinate p) {
        double dx = x - p.x;
        double dy = y - p.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Getter for property x.
     *
     * @return Value for property x.
     */
    public double getX() {
        return x;
    }

    /**
     * Setter for property x.
     *
     * @param x Value to set for property x.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Getter for property y.
     *
     * @return Value for property y.
     */
    public double getY() {
        return y;
    }

    /**
     * Setter for property y.
     *
     * @param y Value to set for property y.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Compares this {@link Coordinate} with the specified {@link Coordinate} for order.
     * Returns:
     * <UL>
     * <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
     * other.y))
     * <LI> 0 : this.x == other.x && this.y = other.y
     * <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
     * <p>
     * </UL>
     * Note: This method assumes that ordinate values
     * are valid numbers.  NaN values are not handled correctly.
     *
     * @param o the <code>Coordinate</code> with which this <code>Coordinate</code>
     *          is being compared
     * @return -1, zero, or 1 as this <code>Coordinate</code>
     * is less than, equal to, or greater than the specified <code>Coordinate</code>
     */
    @Override
    public int compareTo(Coordinate o) {
        Coordinate other = o;

        if (x < other.x) {
            return -1;
        }
        if (x > other.x) {
            return 1;
        }
        if (y < other.y) {
            return -1;
        }
        if (y > other.y) {
            return 1;
        }
        return 0;
    }

    /**
     * Returns a <code>String</code> of the form <I>(x,y)</I> .
     *
     * @return a <code>String</code> of the form <I>(x,y)</I>
     */
    @Override
    public String toString() {
        return String.format("(%f, %f)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Coordinate rhs = (Coordinate) obj;
        return new EqualsBuilder()
                .append(this.x, rhs.x)
                .append(this.y, rhs.y)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(x)
                .append(y)
                .toHashCode();
    }
}