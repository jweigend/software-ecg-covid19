//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.chartng.axis;

import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Dimension2D;
import javafx.geometry.Side;
import javafx.scene.chart.ValueAxis;
import javafx.util.StringConverter;
import javafx.util.converter.TimeStringConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@SuppressWarnings("all")
public class DateAxis extends BaseAxis<Long> {

    private static final int MIN_SPACE_BETWEEN_TICKMARKS = 6;

    private static final double ONE_DAY_IN_MILLIS = 86400000;

    private long transition;

    private SimpleObjectProperty<ZoneId> zoneIdProperty = new SimpleObjectProperty<>();

    /**
     * We use these for auto ranging to pick a user friendly tick unit. (must be increasingly bigger)
     */
    private static final TickUnit[] TICK_UNIT_DEFAULTS = {
            new TickUnit("ss,SSS", 1), // 1 millisecond
            new TickUnit("ss,SS", 10, 10, true), // 10 millisecond
            new TickUnit("ss,S", 100, 10, true), // 100 millisecond
            new TickUnit("HH:mm:ss", 1000, 10, true), // 1 second
            new TickUnit("HH:mm:ss", 15000, 15, true), // 15 second
            new TickUnit("dd.MM. HH:mm:ss", 60000, 4, true), // 1 minute
            new TickUnit("dd.MM. HH:mm:ss", 2 * 60000, 4, true), // 2 minute
            new TickUnit("dd.MM. HH:mm:ss", 5 * 60000, 5, true), // 5 minute
            new TickUnit("dd.MM. HH:mm", ONE_DAY_IN_MILLIS / 24 / 4, 15, true), // 1 quarter hour
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 24 / 2, 2, true), // 1 half hour
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 24, 4, true), // 1 hour
            new TickUnit("dd.MM.yy HH:mm", 60000 * 90, 6, true), // 90 min
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 12, 2, true), // 2 hours
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 8, 3, true), // 3 hours
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 6, 4, true), // 4 hours
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 4, 3, true), // 6 hours
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS / 2, 4, true), // 12 hours
            new TickUnit("dd.MM.yy HH:mm", ONE_DAY_IN_MILLIS, 4, true), // 1 day
            new TickUnit("dd.MM.yy", 2 * ONE_DAY_IN_MILLIS, 2, false), // 2 days
            new TickUnit("dd.MM.yy", 3 * ONE_DAY_IN_MILLIS, 3, false), // 3 days
            new TickUnit("dd.MM.yy", 4 * ONE_DAY_IN_MILLIS, 4, false), // 4 days
            new TickUnit("dd.MM.yy", 5 * ONE_DAY_IN_MILLIS, 5, false), // 5 days
            new TickUnit("dd.MM.yy", 6 * ONE_DAY_IN_MILLIS, 6, false), // 6 days
            new TickUnit("dd.MM.yy", 7 * ONE_DAY_IN_MILLIS, 7, false), // 7 days
            new TickUnit("dd.MM.yy", 8 * ONE_DAY_IN_MILLIS, 8, false), // 8 days
            new TickUnit("dd.MM.yy", 9 * ONE_DAY_IN_MILLIS, 9, false), // 9 days
            new TickUnit("dd.MM.yy", 10 * ONE_DAY_IN_MILLIS, 10, false), // 10 days
            new TickUnit("dd.MM.yy", 15 * ONE_DAY_IN_MILLIS, 15, false), // 15 days
            new TickUnit("dd.MM.yy", 20 * ONE_DAY_IN_MILLIS, 20, false), // 20 days
            new TickUnit("dd.MM.yy", 25 * ONE_DAY_IN_MILLIS, 25, false), // 25 days
            new TickUnit("MMM-yyyy", 31 * ONE_DAY_IN_MILLIS),  // 31 days ~ 1 month
            new TickUnit("MMM-yyyy", 41 * ONE_DAY_IN_MILLIS),  // 41 days
            new TickUnit("MMM-yyyy", 51 * ONE_DAY_IN_MILLIS),  // 51 days
            new TickUnit("MMM-yyyy", 62 * ONE_DAY_IN_MILLIS),  // 62 days ~ 2 months
            new TickUnit("MMM-yyyy", 70 * ONE_DAY_IN_MILLIS),  // 70 days
            new TickUnit("MMM-yyyy", 77 * ONE_DAY_IN_MILLIS),  // 77 days
            new TickUnit("MMM-yyyy", 84 * ONE_DAY_IN_MILLIS),  // 84 days
            new TickUnit("MMM-yyyy", 93 * ONE_DAY_IN_MILLIS),  // 93 days ~ 3 months
            new TickUnit("MMM-yyyy", 108 * ONE_DAY_IN_MILLIS), // 108 days
            new TickUnit("MMM-yyyy", 124 * ONE_DAY_IN_MILLIS), // 124 days ~ 4 months
            new TickUnit("MMM-yyyy", 139 * ONE_DAY_IN_MILLIS), // 139 days
            new TickUnit("MMM-yyyy", 155 * ONE_DAY_IN_MILLIS), // 155 days ~ 5 months
            new TickUnit("MMM-yyyy", 170 * ONE_DAY_IN_MILLIS), // 170 days
            new TickUnit("MMM-yyyy", 186 * ONE_DAY_IN_MILLIS), // 186 days ~ 6 months
            new TickUnit("MMM-yyyy", 274 * ONE_DAY_IN_MILLIS), // 274 days ~ 9 months
            new TickUnit("yyyy",     366 * ONE_DAY_IN_MILLIS,6, false)  // 365 days ~ 1 year
    };


    private Object currentAnimationID;
    private final IntegerProperty currentRangeIndexProperty = new SimpleIntegerProperty(this, "currentRangeIndex", -1);
    private final DefaultFormatter defaultFormatter;

     /**
     * The value between each major tick mark in data units. This is
     * automatically set if we are auto-ranging.
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
        public CssMetaData<DateAxis, Number> getCssMetaData() {
            return StyleableProperties.TICK_UNIT;
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "tickUnitProp";
        }
    };

    // ===============================================================================================================
    //   various constructors for the different use cases
    // ===============================================================================================================

    /**
     * Create a auto-ranging DateAxis
     * with UTC as default time zone
     */
    public DateAxis() {
        setAutoRanging(true);
        setForceZeroInRange(false);
        this.zoneIdProperty = new SimpleObjectProperty(ZoneId.of("UTC"));
        defaultFormatter = new DefaultFormatter(this);
    }


    /**
     * Create a non-auto-ranging DateAxis with the given upper bound, lower
     * bound and tick unit and with UTC as default time zone
     *
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit   The tick unit, ie space between tickmarks
     */
    public DateAxis(double lowerBound, double upperBound, double tickUnit) {
        super(lowerBound, upperBound);
        setTickUnit(tickUnit);
        this.zoneIdProperty = new SimpleObjectProperty(ZoneId.of("UTC"));
        defaultFormatter = new DefaultFormatter(this);
        setTickLabelFormatter(defaultFormatter);
    }

    /**
     * Create a non-auto-ranging DateAxis with the given upper bound, lower
     * bound and tick unit
     *
     * @param axisLabel  The name to display for this axis
     * @param lowerBound The lower bound for this axis, ie min plottable value
     * @param upperBound The upper bound for this axis, ie max plottable value
     * @param tickUnit   The tick unit, ie space between tickmarks
     */
    public DateAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        this(lowerBound, upperBound, tickUnit);
        setLabel(axisLabel);
    }

    //===============================================================================================================
    //   SpawningAxis interface implementation
    //===============================================================================================================

    /* (non-Javadoc)
     * @see e.qaware.ekg.awb.common.ui.chartng.axis.SpawningAxis#spawn()
     */
    @Override
    public SpawningAxis<Long> spawn() {
        DateAxis dateAxis = new DateAxis();
        dateAxis.setTickUnit(this.getTickUnit());
        dateAxis.setLabel(this.getLabel());
        dateAxis.setHeight(this.getHeight());
        dateAxis.setWidth(this.getWidth());
        dateAxis.setVisible(this.isVisible());
        dateAxis.setOpacity(this.getOpacity());

        return dateAxis;
    }

    /* (non-Javadoc)
     * @see e.qaware.ekg.awb.common.ui.chartng.axis.SpawningAxis#spawnBounded()
     */
    @Override
    public SpawningAxis<Long> spawnBounded() {

        DateAxis dateAxis = new DateAxis();
        // style x-axis including behavior in term of scaling the axis
        dateAxis.setAutoRanging(false);
        dateAxis.setTickUnit(this.getTickUnit());
        dateAxis.lowerBoundProperty().bind(this.lowerBoundProperty());
        dateAxis.upperBoundProperty().bind(this.upperBoundProperty());
        dateAxis.setVisible(this.isVisible());
        dateAxis.setOpacity(this.getOpacity());

        return dateAxis;
    }

    //===============================================================================================================
    //   DateAxis API
    //===============================================================================================================

    public void setTimeZone(ZoneId timeZone) {
        this.zoneIdProperty.set(timeZone);
        this.invalidateRange();
        requestAxisLayout();
        layout();
    }

    // ===============================================================================================================
    //   a set of protected methods that will overwrite the behavior of the Axis/ValueAxis
    //   super classes to match the needs to date axis
    // ===============================================================================================================

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override
    protected String getTickMarkLabel(Long value) {
        StringConverter<Long> formatter = getTickLabelFormatter();
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
        return recalculateTicks();
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating()
     * is true then this method should animate the range to the new range.
     *
     * @param range a range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override
    protected void setRange(Object range, boolean animate) {
        final TickProperties rangeProps = (TickProperties) range;
        final double lowerBound = rangeProps.lowerBound;
        final double upperBound = rangeProps.upperBound;
        final double tickUnit = rangeProps.userFriendlyTickUnit;
        final double scale = rangeProps.newScale;
        final double rangeIndex = rangeProps.userFriendlyTickUnitIndex;
        currentRangeIndexProperty.set((int) rangeIndex);
        final double oldLowerBound = getLowerBound();

        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);

        currentLowerBound.set(lowerBound);
        setScale(scale);
    }

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range  A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override
    protected List<Long> calculateTickValues(double length, Object range) {
        final TickProperties rangeProps = (TickProperties) range;
        final double lowerBound = rangeProps.lowerBound;
        final double upperBound = rangeProps.upperBound;
        final double tickUnit = rangeProps.userFriendlyTickUnit;

        List<Long> tickValues = new ArrayList<>();

        if (tickUnit <= 0 || lowerBound == upperBound) {
            tickValues.add((long) lowerBound);

        } else if (getTickUnit() > 0) {
            for (double major = lowerBound; major <= upperBound; major += tickUnit) {
                tickValues.add(convertToLocalTimestamp((long) major));
                if (tickValues.size() > 2000) {
                    // This is a ridiculous amount of major tick marks,
                    // something has probably gone wrong
                    System.err.println("Warning we tried to create more than 2000 major tick marks on a DateAxis. "
                            + "Lower Bound=" + lowerBound + ", Upper Bound=" + upperBound + ", Tick Unit=" + tickUnit);
                    break;
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
    @Override
    protected List<Long> calculateMinorTickMarks() {
        final List<Long> minorTickMarks = new ArrayList<>();
        final double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double tickUnit = getTickUnit();
        final double minorUnit = tickUnit / getMinorTickCount();
        if (getTickUnit() > 0) {
            for (double major = (Math.floor(lowerBound / tickUnit)) * tickUnit; major < upperBound; major += tickUnit) {
                for (double minor = major + minorUnit; minor < (major + tickUnit); minor += minorUnit) {
                    minorTickMarks.add(convertToLocalTimestamp((long) minor));
                    if (minorTickMarks.size() > 10000) {
                        // This is a ridiculous amount of major tick marks,
                        // something has probably gone wrong
                        System.err
                                .println("Warning we tried to create more than 10000 minor tick marks on a DateAxis. "
                                        + "Lower Bound=" + getLowerBound() + ", Upper Bound=" + getUpperBound()
                                        + ", Tick Unit=" + tickUnit);
                        break;
                    }
                }
            }
        }
        return minorTickMarks;
    }

    /**
     * Measure the size of the label for given tick mark value. This uses the
     * font that is set for the tick marks
     *
     * @param value tick mark value
     * @param range range to use during calculations
     * @return size of tick mark label for given value
     */
    @Override
    protected Dimension2D measureTickMarkSize(Long value, Object range) {
        final TickProperties tickProperties = (TickProperties) range;
        return measureTickMarkSize(value, getTickLabelRotation(), tickProperties.userFriendlyTickUnitIndex);
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

        final boolean vertical = Side.LEFT.equals(getSide()) || Side.RIGHT.equals(getSide());

        // check if we need to force zero into range
        if (isForceZeroInRange()) {
            if (maxValue < 0) {
                maxValue = 0;
            } else if (minValue > 0) {
                minValue = 0;
            }
        }

        boolean skipUpperPadding = maxValue == overwriteUpperBound;
        boolean skipLowerPadding = minValue == overwriteLowerBound;

        if (!Double.isNaN(overwriteUpperBound) && maxValue < overwriteUpperBound) {
            skipUpperPadding = true;
            maxValue = overwriteUpperBound;
        }

        if (!Double.isNaN(overwriteLowerBound) && minValue > overwriteLowerBound) {
            skipLowerPadding = true;
            minValue = overwriteLowerBound;
        }


        final double range = maxValue - minValue;

        final double paddedRange;
        final double padding;

        if (skipLowerPadding && skipUpperPadding) {
            paddedRange = range;
            padding = 0;
        } else if (skipLowerPadding || skipUpperPadding) {
            paddedRange = (range == 0) ? 1 : Math.abs(range) * 1.01;
            padding = paddedRange - range;
        } else {
            paddedRange = (range == 0) ? 2 : Math.abs(range) * 1.02;
            padding = (paddedRange - range) / 2;
        }

        // if min and max are not zero then add padding to them
        double paddedMin = skipLowerPadding ? minValue : minValue - padding;
        double paddedMax = skipUpperPadding ? maxValue : maxValue + padding;

        // check padding has not pushed min or max over zero line
        if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMin = 0;
        }
        if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
            // padding pushed min above or below zero so clamp to 0
            paddedMax = 0;
        }
        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int) Math.floor(Math.abs(length) / labelSize);

        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);

        // calculate tick unit for the number of ticks can have in the given data range
        double calculatedTickUnit = paddedRange / (double) numOfTickMarks;

        return calculateTickProperties(length, vertical, paddedMin, paddedMax, calculatedTickUnit);
    }



    // ===============================================================================================================
    //   the sophicated internal ranch caluclation of this date axis
    // ===============================================================================================================


    private long convertToLocalTimestamp(long utcTimestamp) {
        return utcTimestamp;
    }

    private TickProperties recalculateTicks() {
        final Side side = getSide();
        final boolean vertical = Side.LEFT.equals(side) || Side.RIGHT.equals(side);
        final double length = vertical ? getHeight() : getWidth();
        // guess a sensible starting size for label size, that is approx 2 lines
        // vertically or 2 charts horizontally
        double labelSize = getTickLabelFont().getSize() * 2;

        double range = getUpperBound() - getLowerBound();

        // calculate the number of tick-marks we can fit in the given length
        int numOfTickMarks = (int) Math.floor(Math.abs(length) / labelSize);

        // can never have less than 2 tick marks one for each end
        numOfTickMarks = Math.max(numOfTickMarks, 2);

        // calculate tick unit for the number of ticks can have in the given
        // data range
        double calculatedTickUnit = range / (double) numOfTickMarks;

        TickProperties tickProperties = calculateTickProperties(length, vertical, getLowerBound(), getUpperBound(), calculatedTickUnit);

        // !!!!! changes the axis properties
        currentRangeIndexProperty.set(tickProperties.userFriendlyTickUnitIndex);
        setTickUnit(tickProperties.userFriendlyTickUnit);
        setMinorTickCount(TICK_UNIT_DEFAULTS[tickProperties.userFriendlyTickUnitIndex].minorTickCount);

        return tickProperties;
    }


    private TickProperties calculateTickProperties(double length, boolean vertical, double paddedMin,
                                                                double paddedMax, double calculatedTickUnit) {

        // search for the best tick unit that fits
        int countMajorTicks = 0;
        int rangeIndex = 10;
        double maxRounded = 0;
        double minRounded = 0;
        double userFriendlyTickUnit = 0;
        double reqLength = Double.MAX_VALUE;

        // loop till we find a set of ticks that fit length and result in a
        // total of less than 20 tick marks
        while (reqLength > length || countMajorTicks > 20) {

            // find a user friendly match from our default tick units to match
            // calculated tick unit. The block will loop until the next larger tick unit than the calculated one
            // is resolved. It must be larger to ensure the label fit into it.
            for (int i = 0; i < TICK_UNIT_DEFAULTS.length; i++) {
                double tickUnitDefault = TICK_UNIT_DEFAULTS[i].tickUnit;
                if (tickUnitDefault > calculatedTickUnit) {
                    rangeIndex = i;
                    userFriendlyTickUnit = tickUnitDefault;
                    break;
                }
            }

            setMinorTickCount(TICK_UNIT_DEFAULTS[rangeIndex].minorTickCount);

            // move min and max to nearest tick mark
            minRounded = Math.floor(paddedMin / userFriendlyTickUnit) * userFriendlyTickUnit;
            maxRounded = Math.ceil(paddedMax / userFriendlyTickUnit) * userFriendlyTickUnit;

            // calculate the required length to display the chosen tick marks for real, this will handle if
            // there are huge numbers involved etc or special formatting of the tick mark label text
            countMajorTicks = 0;
            double last = 0;
            double minGapBetweenMajorTicks = 0;

            double[] calulatedTickProperties = getMaxReqTickGap(minRounded, maxRounded, userFriendlyTickUnit, vertical, rangeIndex);
            countMajorTicks = (int)calulatedTickProperties[0];
            minGapBetweenMajorTicks = calulatedTickProperties[1];

            reqLength = (countMajorTicks - 1) * minGapBetweenMajorTicks;
            calculatedTickUnit = userFriendlyTickUnit;
            // check if we already found max tick unit
            if (userFriendlyTickUnit == TICK_UNIT_DEFAULTS[TICK_UNIT_DEFAULTS.length - 1].tickUnit) {
                // nothing we can do so just have to use this
                break;
            }
        }

        // calculate new scale
        final double newScale = calculateNewScale(length, minRounded, maxRounded);

        // return new range
        return new TickProperties(minRounded, maxRounded, userFriendlyTickUnit, newScale, rangeIndex);
    }

    private double getTickUnit() {
        return tickUnitProp.get();
    }

    private void setTickUnit(double value) {
        tickUnitProp.set(value);
    }

    private DoubleProperty tickUnitProperty() {
        return tickUnitProp;
    }

    /**
     * Calculates the amount of major ticks based on the given axis properties and
     * also the required minium space between the major ticks so that all tick marks will fit into it without
     * any problems.
     * The result will of two values (cound and gap) will returned as double array.
     *
     * @param minRounded the rounded axis value of the lower bound major tick (calcualted by the caller)
     * @param maxRounded the rounded axis value of the upper bound major tick (calcualted by the caller)
     * @param userFriendlyTickUnit the tick unit (gap between major ticks) that should used as default
     * @param vertical flat if the axis is vertical or horizontal (this controls if width or heigt of the marks will messured)
     * @param rangeIndex the index of the user friend formatted TickUnit selected by the caller
     * @return an double[] with the the count of mayor ticks as first value and required minimum gap between the major ticks as second
     */
    private double[] getMaxReqTickGap(double minRounded, double maxRounded, double userFriendlyTickUnit,
                                      boolean vertical, int rangeIndex) {

        double minRequiredMajorTickGab = 0;
        double lastHalfTickMarkSize = 0;
        double count = 0;

        // iterate over each major tick, take a look to the tick mark label size
        for (double major = minRounded; major <= maxRounded; major += userFriendlyTickUnit, count++) {
            double currentTickMarkSize = (vertical)
                    ? measureTickMarkSize((long) major, getTickLabelRotation(), rangeIndex).getHeight()
                    : measureTickMarkSize((long) major, getTickLabelRotation(), rangeIndex).getWidth();

            if (major == minRounded) { // first major tick - no min gap can defined in this loop
                lastHalfTickMarkSize = currentTickMarkSize / 2;

            } else {
                // if the current tick mark needs more space use take it requirements as new gap, else the previous one.
                minRequiredMajorTickGab = Math.max(minRequiredMajorTickGab,
                        lastHalfTickMarkSize + MIN_SPACE_BETWEEN_TICKMARKS + (currentTickMarkSize / 2));
            }
        }

        // return the amount of mayor ticks and required minimum gap between the major ticks
        return new double[] {count, minRequiredMajorTickGab};
    }


    private long getTransitionTimeZone(double tickUnit) {
        if (tickUnit >= 86400000 / 12 && tickUnit <= 86400000) {
            return transition;
        }
        return 0;
    }

    public void setTransitionTimeZone(String timezone) {
        GregorianCalendar calendar = new GregorianCalendar(1970, 0, 1, 0, 0, 0); // year,
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        transition = calendar.getTimeInMillis();
    }



    /**
     * Measure the size of the label for given tick mark value. This uses the
     * font that is set for the tick marks
     *
     * @param value      tick mark value
     * @param rotation   The text rotation
     * @param rangeIndex The index of the tick unit range
     * @return size of tick mark label for given value
     */
    private Dimension2D measureTickMarkSize(Long value, double rotation, int rangeIndex) {
        String labelText;
        StringConverter<Long> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        if (formatter instanceof DefaultFormatter) {
            labelText = ((DefaultFormatter) formatter).toString(value, rangeIndex);
        } else {
            labelText = formatter.toString(value);
        }
        return measureTickMarkLabelSize(labelText, rotation);
    }


    // -------------------------------------------------------------------------------------------------------------
    // STYLESHEET HANDLING
    // -------------------------------------------------------------------------------------------------------------

    /**
     * @return The CssMetaData associated with this class, which may include the CssMetaData of its super classes.
     *
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }


    /**
     * implementation detail
     */
    private static class StyleableProperties {

        private static final CssMetaData<DateAxis, Number> TICK_UNIT = new CssMetaData<DateAxis, Number>(
                "-fx-tick-unit", SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(DateAxis n) {
                return n.tickUnitProp == null || !n.tickUnitProp.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Number> getStyleableProperty(DateAxis n) {
                return (StyleableProperty<Number>) n.tickUnitProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(
                    ValueAxis.getClassCssMetaData());
            styleables.add(TICK_UNIT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }


    // -------------------------------------------------------------------------------------------------------------
    //  inner class formatter
    // -------------------------------------------------------------------------------------------------------------

    /**
     * Default number formatter for DateAxis, this stays in sync with
     * auto-ranging and formats values appropriately. You can wrap this
     * formatter to add prefixes or suffixes;
     *
     * @since JavaFX 2.0
     */
    private static class DefaultFormatter extends StringConverter<Long> {

        private TimeStringConverter formatter;

        private String prefix = null;

        private String suffix = null;

        private final Date tempDate = new Date();

        private final ReadOnlyProperty<ZoneId> zoneIdProperty;


        /**
         * Construct a DefaultFormatter for the given DateAxis
         *
         * @param axis The axis to format tick marks for
         */
        private DefaultFormatter(final DateAxis axis) {
            this.zoneIdProperty = axis.zoneIdProperty;
            this.formatter = createFormatter(axis.isAutoRanging() ? axis.currentRangeIndexProperty.get() : -1);

            axis.currentRangeIndexProperty.addListener((observable, oldValue, newValue) -> {
                formatter = createFormatter(axis.currentRangeIndexProperty.get());
            });

            zoneIdProperty.addListener((observable) -> {
                formatter = createFormatter(axis.currentRangeIndexProperty.get());
            });
        }

        /**
         * Construct a DefaultFormatter for the given DateAxis with a prefix
         * and/or suffix.
         *
         * @param axis   The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can
         *               be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can
         *               be null if not needed
         */
        public DefaultFormatter(DateAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        private TimeStringConverter getFormatter(int rangeIndex) {
            return formatter;
        }

        private TimeStringConverter createFormatter(int rangeIndex) {

            DateFormat dateFormat = null;

            if (rangeIndex < 0) {
                dateFormat = new SimpleDateFormat("dd.MM.yy");
            } else if (rangeIndex >= TICK_UNIT_DEFAULTS.length) {
                dateFormat = new SimpleDateFormat(TICK_UNIT_DEFAULTS[TICK_UNIT_DEFAULTS.length - 1].tickUnitFormatter);
            } else {
                dateFormat = new SimpleDateFormat(TICK_UNIT_DEFAULTS[rangeIndex].tickUnitFormatter);
            }

            dateFormat.setTimeZone(TimeZone.getTimeZone(zoneIdProperty.getValue()));
            return new TimeStringConverter(dateFormat);
        }

        /**
         * Converts the object provided into its string form. Format of the
         * returned string is defined by this converter.
         *
         * @return a string representation of the object passed in.
         * @see StringConverter#toString
         */
        @Override
        public String toString(Long object) {
            return toString(object, formatter);
        }

        private String toString(Long object, int rangeIndex) {
            return toString(object, getFormatter(rangeIndex));
        }

        private String toString(Long object, TimeStringConverter formatter) {
            tempDate.setTime(object);
            if (prefix != null && suffix != null) {
                return prefix + formatter.toString(tempDate) + suffix;
            } else if (prefix != null) {
                return prefix + formatter.toString(tempDate);
            } else if (suffix != null) {
                return formatter.toString(tempDate) + suffix;
            } else {
                return formatter.toString(tempDate);
            }
        }

        /**
         * Converts the String provided into a Number defined by the this
         * converter. Format of the string and type of the resulting object is
         * defined by this converter.
         *
         * @return a Number representation of the string passed in.
         * @see StringConverter#toString
         */
        @Override
        public Long fromString(String string) {
            int prefixLength = (prefix == null) ? 0 : prefix.length();
            int suffixLength = (suffix == null) ? 0 : suffix.length();
            return formatter.fromString(string.substring(prefixLength, string.length() - suffixLength)).getTime();
        }
    }


    private static class TickUnit {
        final double tickUnit;
        final String tickUnitFormatter;
        int minorTickCount = 5;
        boolean beAwareOfTimeZone = true;

        TickUnit(String tickFormatter, double tickUnit) {
            this.tickUnit = tickUnit;
            this.tickUnitFormatter = tickFormatter;

        }

        TickUnit(String tickFormatter, double tickUnit, int minorTickCount, boolean beAwareOfTimeZone) {
            this(tickFormatter, tickUnit);
            this.minorTickCount = minorTickCount;
            this.beAwareOfTimeZone = beAwareOfTimeZone;
        }
    }

    private static class TickProperties {
        final double userFriendlyTickUnit;

        final int userFriendlyTickUnitIndex;

        final double lowerBound;

        final double upperBound;

        final double newScale;

        private TickProperties(double lowerBound, double upperBound, double userFriendlyTickUnit,
                               double newScale, int userFriendlyTickUnitIndex) {

            this.userFriendlyTickUnit = userFriendlyTickUnit;
            this.userFriendlyTickUnitIndex = userFriendlyTickUnitIndex;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.newScale = newScale;
        }
    }
}
