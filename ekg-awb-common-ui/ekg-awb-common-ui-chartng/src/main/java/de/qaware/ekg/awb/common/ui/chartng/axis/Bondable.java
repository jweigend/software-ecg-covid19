package de.qaware.ekg.awb.common.ui.chartng.axis;

import javafx.beans.property.DoubleProperty;

/**
 * An interface that represents chart axis implementation's
 * that can bind to others bondable axis and can scale together without loosing
 * the own value ranges.
 */
public interface Bondable {

    /**
     * Returns the value for the lower bound of this axis (minimum value).
     * This is automatically set if auto ranging is on.
     *
     * @return the lower bound property of the axis
     */
    DoubleProperty lowerBoundProperty();

    /**
     * Returns the value for the upper bound of this axis (maximum value).
     * This is automatically set if auto ranging is on.
     *
     * @return the upper bound property of the axis
     */
    DoubleProperty upperBoundProperty();

    /**
     * Returns an upper bound value that will overwrite the
     * max data value that needs to be plotted on this axis.
     *
     * It can used to ensure the a axis as the same range as other
     * axis independent of it's plotted values.
     *
     * @return the upper bound value that is enforced for this axis or Double.NaN if not defined.
     */
    double getEnforcedUpperBound();

    /**
     * Returns a lower bound value that will overwrite the
     * min data value that needs to be plotted on this axis.
     *
     * It can used to ensure the a axis as the same range as other
     * axis independent of it's plotted values.
     *
     * @return the minimum bound value that is enforced for this axis or Double.NaN if not defined.
     */
    double getEnforcedLowerBound();

    /**
     * Sets an upper bound value for the axis that will overwrite
     * the max data value that needs to be plotted on this axis.
     *
     * This will cause that the axis plotted range will not stop at the highest value if it is lower.
     * If the enforced upper bound is lower than the max value to plot the enforced maximum has
     * no effect.
     * With other words: highest value will wins and it's guaranteed that is never smaller than
     * the enforced upper bound.
     *
     * @param upperBound the upper bound value that will overwrite the plotted max value if higher
     */
    void forceUpperBound(double upperBound);

    /**
     * Sets an lower bound value for the axis that will overwrite
     * the min data value that needs to be plotted on this axis.
     *
     * This will cause that the axis plotted range will not stop at the minimum value if it is higher
     * than the enforced value.
     * If the enforced lower bound is higher than the min value to plot, it's free to the implementation
     * if it has an effect or not.
     *
     * At date axis cutting the highest value didn't make sense, but at value (y) axis this can be the case.
     *
     * @param lowerBound the lower bound value that will overwrite the plotted max value if lower
     */
    void forceLowerBound(double lowerBound);

    /**
     * Invalidates the layout state of the axis and enforce the new calculation of
     * the axis ranges/layout.
     * If enforced upper/lower bound is set this will be used for the layout calculation
     * for example by overwrite fixed axis ranges set by other APIs.
     */
    void layoutCustomBounds();
}
