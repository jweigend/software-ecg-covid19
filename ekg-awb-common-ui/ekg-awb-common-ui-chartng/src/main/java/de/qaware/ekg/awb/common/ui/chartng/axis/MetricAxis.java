/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package de.qaware.ekg.awb.common.ui.chartng.axis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.scene.chart.ValueAxis;
import javafx.util.StringConverter;
import org.apache.commons.lang3.NotImplementedException;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A axis class that plots a range of numbers with major tick marks every "tickUnitProp". You can use any Number type with
 * this axis, Long, Double, BigDecimal etc.
 */
@SuppressWarnings("all")
public class MetricAxis extends BaseAxis<Double> {

    private Object currentAnimationID;

    private final StringProperty currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");

    private final DefaultFormatter defaultFormatter = new DefaultFormatter(this);


    /**
     * The value between each major tick mark in data units. This is automatically set if we are auto-ranging.
     */
    private final DoubleProperty tickUnitProp = new StyleableDoubleProperty(5) {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public CssMetaData<MetricAxis, Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return MetricAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnitProp";
        }
    };

    /**
     * The scaleProp factor from data units to visual units
     */
    private final ReadOnlyDoubleWrapper scaleProp = new ReadOnlyDoubleWrapper(this, "scaleProp", 0) {
        @Override
        protected void invalidated() {
            requestAxisLayout();
        }
    };

    //=================================================================================================================
    // various constructors
    //=================================================================================================================

    /**
     * Create a auto-ranging NumberAxis
     */
    public MetricAxis() {
    }

    /**
     * Create a non-auto-ranging NumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit   The tick unit, ie space between tickmarks
     */
    public MetricAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
    }

    /**
     * Create a non-auto-ranging NumberAxis with the given upper bound, lower bound and tick unit
     *
     * @param axisLabel  The name to display for this axis
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit   The tick unit, ie space between tickmarks
     */
    public MetricAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
        setLabel(axisLabel);
    }

    //=================================================================================================================
    // MetricAxis API implementation
    //=================================================================================================================

    /**
     * Returns the tick unit
     * @return the tick unit
     */
    public final double getTickUnit() {
        return tickUnitProp.get();
    }

    /**
     * Sets the tick unit
     * @param value the tick unit
     */
    public final void setTickUnit(double value) {
        tickUnitProp.set(value);
    }

    /**
     * Returns the tick unit property
     * @return the tick unit property
     */
    public final DoubleProperty tickUnitProperty() {
        return tickUnitProp;
    }

    @Override
    public SpawningAxis<Double> spawn() {
        MetricAxis metricAxis = new MetricAxis();
        metricAxis.setLabel(this.getLabel());
        metricAxis.setSide(this.getSide());
        metricAxis.setVisible(this.isVisible());
        metricAxis.setOpacity(this.getOpacity());
        metricAxis.setUpperBound(this.getUpperBound());
        metricAxis.setLowerBound(this.getLowerBound());
        metricAxis.setHeight(this.getHeight());
        metricAxis.setWidth(this.getWidth());
        metricAxis.setTickLength(this.getMinorTickLength());
        metricAxis.setMinorTickLength(this.getMinorTickLength());
        metricAxis.setForceZeroInRange(this.isForceZeroInRange());
        metricAxis.forceLowerBound(this.getEnforcedLowerBound());
        metricAxis.forceUpperBound(this.getEnforcedUpperBound());
        return metricAxis;
    }

    @Override
    public SpawningAxis<Double> spawnBounded() {
        throw new NotImplementedException("");
    }


    // -------------- PROTECTED METHODS --------------------------------------------------------------------------------

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override
    protected String getTickMarkLabel(Double value) {
        StringConverter<Double> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        return formatter.toString(value);
    }

    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
    @Override
    protected Object getRange() {
        double tickUnit = recalculateTickUnit();
        return new Object[]{
                getLowerBound(),
                getUpperBound(),
                tickUnit,
                getScale(),
                currentFormatterProperty.get()
        };
    }

    private double recalculateTickUnit() {
        final Side side = getSide();
        final boolean vertical = Side.LEFT.equals(side) || Side.RIGHT.equals(side);
        final double length = vertical ? getHeight() : getWidth();
        // guess a sensible starting size for label size, that is approx 2 lines
        // vertically or 2 charts horizontally
        double labelSize = getTickLabelFont().getSize() * 2;

        double currentRange = getUpperBound() - getLowerBound();

        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int) Math.floor(Math.abs(length) / labelSize);
        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);
        // calculate tick unit for the number of ticks can have in the given
        // data range
        double tickUnit = currentRange / (double) numOfTickMarks;

        int exp = (int) Math.floor(Math.log10(tickUnit));
        final double mant = tickUnit / Math.pow(10, exp);

        int minorTicks = 8;
        while (mant * minorTicks > 10) {
            minorTicks /= 2;
        }

        tickUnit = Math.pow(10, exp + 1) / minorTicks;

        setTickUnit(tickUnit);

        return tickUnit;
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range   A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override
    protected void setRange(Object range, boolean animate) {
        final Object[] rangeProps = (Object[]) range;
        final double lowerBound = (Double) rangeProps[0];
        final double upperBound = (Double) rangeProps[1];
        final double tickUnit = (Double) rangeProps[2];
        final double scale = (Double) rangeProps[3];
        final String formatter = (String) rangeProps[4];
        currentFormatterProperty.set(formatter);
        final double oldLowerBound = getLowerBound();
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);
        currentLowerBound.set(lowerBound);
        setScale(scale);
    }


    ReadOnlyDoubleWrapper scalePropertyImpl() {
        return scaleProp;
    }

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range  A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override
    protected List<Double> calculateTickValues(double length, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final double lowerBound = (Double) rangeProps[0];
        final double upperBound = (Double) rangeProps[1];
        final double tickUnit = (Double) rangeProps[2];
        List<Double> tickValues = new ArrayList<>();
        if (lowerBound == upperBound) {
            tickValues.add(lowerBound);
        } else if (tickUnit <= 0) {
            tickValues.add(lowerBound);
            tickValues.add(upperBound);
        } else if (tickUnit > 0) {
            if (((upperBound - lowerBound) / tickUnit) > 2000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 2000 major tick marks on a NumberAxis. " +
                        "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit);
            } else {
                if (lowerBound + tickUnit < upperBound) {
                    // If tickUnitProp is integer, start with the nearest integer
                    double major = Math.rint(tickUnit) == tickUnit ? Math.ceil(lowerBound) : lowerBound + tickUnit;
                    // Round to tick unit values
                    major = roundToTickUnit(major, tickUnit);
                    int count = (int) Math.ceil((upperBound - major) / tickUnit);
                    for (int i = 0; major < upperBound && i < count; major += tickUnit, i++) {
                        if (!tickValues.contains(major)) {
                            tickValues.add(major);
                        }
                    }
                }
            }
        }
        return tickValues;
    }

    /**
     * Calculate a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    protected List<Double> calculateMinorTickMarks() {
        final List<Double> minorTickMarks = new ArrayList<>();
        final double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double tickUnit = getTickUnit();
        final double minorUnit = tickUnit / Math.max(1, getMinorTickCount());
        if (tickUnit > 0) {
            if (((upperBound - lowerBound) / minorUnit) > 10000) {
                // This is a ridiculous amount of major tick marks, something has probably gone wrong
                System.err.println("Warning we tried to create more than 10000 minor tick marks on a NumberAxis. " +
                        "Lower Bound=" + getLowerBound() + ", Upper Bound=" + getUpperBound() + ", Tick Unit=" + tickUnit);
                return minorTickMarks;
            }
            final boolean tickUnitIsInteger = Math.rint(tickUnit) == tickUnit;
            if (tickUnitIsInteger) {
                double minor = Math.floor(lowerBound) + minorUnit;
                // Round to tick unit values
                minor = roundToTickUnit(minor, tickUnit);
                int count = (int) Math.ceil((Math.ceil(lowerBound) - minor) / minorUnit);
                for (int i = 0; minor < Math.ceil(lowerBound) && i < count; minor += minorUnit, i++) {
                    if (minor > lowerBound) {
                        minorTickMarks.add(minor);
                    }
                }
            }
            double major = tickUnitIsInteger ? Math.ceil(lowerBound) : lowerBound;
            // Round to tick unit values
            major = roundToTickUnit(major, tickUnit);
            int count = (int) Math.ceil((upperBound - major) / tickUnit);
            for (int i = 0; major < upperBound && i < count; major += tickUnit, i++) {
                final double next = Math.min(major + tickUnit, upperBound);
                double minor = major + minorUnit;
                int minorCount = (int) Math.ceil((next - minor) / minorUnit);
                for (int j = 0; minor < next && j < minorCount; minor += minorUnit, j++) {
                    minorTickMarks.add(minor);
                }
            }
        }
        return minorTickMarks;
    }

    private double roundToTickUnit(double major, double tickUnit) {
        return Math.floor((major + tickUnit / 2.0) / tickUnit) * tickUnit;
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    @Override
    protected Dimension2D measureTickMarkSize(Double value, Object range) {
        final Object[] rangeProps = (Object[]) range;
        final String formatter = (String) rangeProps[4];
        return measureTickMarkSize(value, getTickLabelRotation(), formatter);
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the font that is set for the tick marks
     *
     * @param value        tick mark value
     * @param rotation     The text rotation
     * @param numFormatter The number formatter
     * @return size of tick mark label for given value
     */
    private Dimension2D measureTickMarkSize(Double value, double rotation, String numFormatter) {
        String labelText;
        StringConverter<Double> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        if (formatter instanceof DefaultFormatter) {
            labelText = ((DefaultFormatter) formatter).toString(value, numFormatter);
        } else {
            labelText = formatter.toString(value);
        }
        return measureTickMarkLabelSize(labelText, rotation);
    }

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged
     *
     * @param minValue  The min data value that needs to be plotted on this axis
     * @param maxValue  The max data value that needs to be plotted on this axis
     * @param length    The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     */
    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {

        // which side is the axis (horizontal, vertical, left , right)
        final Side side = getSide();

        // check if we need to force zero into range
        if (isForceZeroInRange()) {
            if (maxValue < 0) {
                maxValue = 0;

            } else if (minValue > 0) {
                minValue = 0;
            }
        }

        if (!Double.isNaN(overwriteUpperBound) && overwriteUpperBound > maxValue) {
            maxValue = overwriteUpperBound / 1.01;
        }

        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int) Math.floor(length / labelSize);

        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);
        int minorTickCount = Math.max(getMinorTickCount(), 1);


        // check if range is a positive value but smaller than the precion
        // of a double and can't divided into multiple segements (ticks)
        double range = maxValue - minValue;
        if (range != 0 && range / (numOfTickMarks * minorTickCount) <= Math.ulp(minValue)) {
            range = 0;
        }

        // pad min and max by 2%, checking if the range is zero
        final double paddedRange;
        if (range == 0) {
            // range = 0 -> graph is a horizontal line -> take a padding of 2 if line
            // at y=0 or 2% of the y-value if plot somewhere else
            paddedRange = (minValue == 0) ? 2 : Math.abs(minValue) * 0.02;
        } else {
            // graph has multiple y-values, take this range + 2% padding
            paddedRange = Math.abs(range) * 1.02;
        }

        // calc the absolute padding for one side (upper and buttom side) that will be 1% of the range
        final double padding = (paddedRange - range) / 2;

        // add padding to min and max
        double paddedMin = minValue - padding;
        double paddedMax = maxValue + padding;

        // check padding has not pushed min or max over zero line
        if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMin = 0;
        }

        if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMax = 0;
        }

        // calculate tick unit for the number of ticks can have in the given data range
        double tickUnit = paddedRange / (double) numOfTickMarks;

        // search for the best tick unit that fits
        double tickUnitRounded = 0;
        double minRounded = 0;
        double maxRounded = 0;

        int count = 0;
        double reqLength = Double.MAX_VALUE;
        String formatter = "#";
        // loop till we find a set of ticks that fit length and result in a total of less than 20 tick marks
        while (reqLength > length || count > 20) {
            int exp = (int) Math.floor(Math.log10(tickUnit));
            final double mant = tickUnit / Math.pow(10, exp);
            double ratio = mant;
            if (mant > 5d) {
                exp++;
                ratio = 1;
            } else if (mant > 1d) {
                ratio = mant > 2.5 ? 5 : 2.5;
            }
            if (exp > 1) {
                formatter = "#,###";
            } else if (exp == 1) {
                formatter = "#";
            } else {
                final boolean ratioHasFrac = Math.rint(ratio) != ratio;
                final StringBuilder formatterB = new StringBuilder("0");
                int n = ratioHasFrac ? Math.abs(exp) + 1 : Math.abs(exp);
                if (n > 0) {
                    formatterB.append(".");
                }
                for (int i = 0; i < n; ++i) {
                    formatterB.append("0");
                }
                formatter = formatterB.toString();

            }
            tickUnitRounded = ratio * Math.pow(10, exp);
            // move min and max to nearest tick mark
            minRounded = Math.floor(paddedMin / tickUnitRounded) * tickUnitRounded;
            maxRounded = Math.ceil(paddedMax / tickUnitRounded) * tickUnitRounded;
            // calculate the required length to display the chosen tick marks for real, this will handle if there are
            // huge numbers involved etc or special formatting of the tick mark label text
            double maxReqTickGap = 0;
            double last = 0;
            count = (int) Math.ceil((maxRounded - minRounded) / tickUnitRounded);
            double major = minRounded;
            for (int i = 0; major <= maxRounded && i < count; major += tickUnitRounded, i++) {
                Dimension2D markSize = measureTickMarkSize(major, getTickLabelRotation(), formatter);
                double size = side.isVertical() ? markSize.getHeight() : markSize.getWidth();
                if (i == 0) { // first
                    last = size / 2;
                } else {
                    maxReqTickGap = Math.max(maxReqTickGap, last + 6 + (size / 2));
                }
            }
            reqLength = (count - 1) * maxReqTickGap;
            tickUnit = tickUnitRounded;

            // fix for RT-35600 where a massive tick unit was being selected
            // unnecessarily. There is probably a better solution, but this works
            // well enough for now.
            if (numOfTickMarks == 2 && reqLength > length) {
                break;
            }
            if (reqLength > length || count > 20) {
                tickUnit *= 2; // This is just for the while loop, if there are still too many ticks
            }
        }

        // calculate new scaleProp
        final double newScale = calculateNewScale(length, minRounded, maxRounded);
        // return new range
        return new Object[]{minRounded, maxRounded, tickUnitRounded, newScale, formatter};
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    /**
     * implementation detail
     */
    private static class StyleableProperties {
        private static final CssMetaData<MetricAxis, Number> TICK_UNIT =
               new CssMetaData<MetricAxis, Number>("-fx-tick-unit", SizeConverter.getInstance(), 5.0) {

                    @Override
                    public boolean isSettable(MetricAxis n) {
                        return n.tickUnitProp == null || !n.tickUnitProp.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(MetricAxis n) {
                        return (StyleableProperty<Number>) (WritableValue<Number>) n.tickUnitProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(ValueAxis.getClassCssMetaData());
            styleables.add(TICK_UNIT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return MetricAxis.StyleableProperties.STYLEABLES;
    }

    // -------------- INNER CLASSES ------------------------------------------------------------------------------------

    /**
     * Default number formatter for NumberAxis, this stays in sync with auto-ranging and formats values appropriately.
     * You can wrap this formatter to add prefixes or suffixes;
     *
     * @since JavaFX 2.0
     */
    public static class DefaultFormatter extends StringConverter<Double> {
        private DecimalFormat formatter;
        private String prefix = null;
        private TickUnitSuffix suffix = null;
        private double div = 1.0;



        /**
         * Construct a DefaultFormatter for the given NumberAxis
         *
         * @param axis The axis to format tick marks for
         */
        public DefaultFormatter(final MetricAxis axis) {

            setUpFormatter(axis);

            final ChangeListener<Object> axisListener = (observable, oldValue, newValue) -> {

                setUpFormatter(axis);

                axis.tickUnitProperty().addListener((o, oV, nV) -> {
                    if (nV.doubleValue() > 2.500E12) {
                        suffix = TickUnitSuffix.TERA;
                        div = 1.000E12;
                    } else if (nV.doubleValue() > 2.500E9) {
                        suffix = TickUnitSuffix.GIGA;
                        div = 1.000E9;
                    } else if (nV.doubleValue() > 2.500E6) {
                        suffix = TickUnitSuffix.MILLION;
                        div = 1.000E6;
                    } else if (nV.doubleValue() > 2.500E3) {
                        suffix = TickUnitSuffix.KILO;
                        div = 1.000E3;
                    } else {
                        suffix = null;
                        div = 1.0;
                    }
                });
            };

            axis.currentFormatterProperty.addListener(axisListener);
            axis.autoRangingProperty().addListener(axisListener);
        }

        private void setUpFormatter(MetricAxis axis) {
            if (axis.isAutoRanging()) {
                formatter = new DecimalFormat(axis.currentFormatterProperty.get());
            } else {
                formatter = new DecimalFormat();
            }
        }

        /**
         * Construct a DefaultFormatter for the given NumberAxis with a prefix and/or suffix.
         *
         * @param axis   The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can be null if not needed
         */
        public DefaultFormatter(MetricAxis axis, String prefix, TickUnitSuffix suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * Converts the object provided into its string form.
         * Format of the returned string is defined by this converter.
         *
         * @return a string representation of the object passed in.
         * @see StringConverter#toString
         */
        @Override
        public String toString(Double object) {
            return toString(object, formatter);
        }

        private String toString(Double object, String numFormatter) {
            if (numFormatter == null || numFormatter.isEmpty()) {
                return toString(object, formatter);
            } else {
                return toString(object, new DecimalFormat(numFormatter));
            }
        }

        private String toString(Double metricValue, DecimalFormat formatter) {

            if (prefix != null && suffix != null) {
                return prefix + formatter.format(metricValue / div) + suffix.getLabel();

            } else if (prefix != null) {
                return prefix + formatter.format(metricValue / div);

            } else if (suffix != null) {

                double currentDivider = div;
                int tickUnitSuffixOrdinal = suffix.ordinal();

                while (metricValue / currentDivider >= 1000 && 0 < tickUnitSuffixOrdinal) {
                    currentDivider *= 1000.0;
                    tickUnitSuffixOrdinal--;
                }


                return formatter.format(metricValue / currentDivider) + TickUnitSuffix.values()[tickUnitSuffixOrdinal].getLabel();

            } else {
                return formatter.format(metricValue / div);
            }
        }




        /**
         * Converts the string provided into a Double defined by the this converter.
         * Format of the string and type of the resulting object is defined by this converter.
         *
         * @return a Double representation of the string passed in.
         * @see StringConverter#toString
         */
        @Override
        public Double fromString(String string) {
            try {
                int prefixLength = (prefix == null) ? 0 : prefix.length();
                int suffixLength = (suffix == null) ? 0 : suffix.getLabel().length();
                return (Double) formatter.parse(string.substring(prefixLength, string.length() - suffixLength));
            } catch (ParseException e) {
                return null;
            }
        }
    }

    private static enum TickUnitSuffix {
        TERA(" T"),
        GIGA(" G"),
        MILLION(" M"),
        KILO(" K");

        private String suffix;

        TickUnitSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getLabel() {
            return suffix;
        }
    }
}