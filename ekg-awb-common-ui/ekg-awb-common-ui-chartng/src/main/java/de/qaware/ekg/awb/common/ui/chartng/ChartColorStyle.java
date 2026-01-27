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
package de.qaware.ekg.awb.common.ui.chartng;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Management for the colors of series
 * Series with the same name will result in the same color
 */
@Singleton
public final class ChartColorStyle {

    private static final Map<String, Color> CHART_COLOR_MAP = new HashMap<>();
    private static final Color[] AVAILABLE_COLORS = {
            Color.DARKSEAGREEN,
            Color.DARKSLATEBLUE,
            Color.DARKSLATEGRAY,
            Color.BLACK,
            Color.BLUE,
            Color.BLUEVIOLET,
            Color.MAROON,
            Color.BROWN,
            Color.MEDIUMBLUE,
            Color.CADETBLUE,
            Color.MEDIUMORCHID,
            Color.CHOCOLATE,
            Color.MEDIUMSEAGREEN,
            Color.MEDIUMSLATEBLUE,
            Color.CRIMSON,
            Color.MEDIUMVIOLETRED,
            Color.MIDNIGHTBLUE,
            Color.DARKBLUE,
            Color.DARKGOLDENROD,
            Color.DARKGREEN,
            Color.NAVY,
            Color.OLIVE,
            Color.DARKOLIVEGREEN,
            Color.OLIVEDRAB,
            Color.DARKORANGE,
            Color.DARKORCHID,
            Color.ORANGERED,
            Color.DARKRED,
            Color.PALEVIOLETRED,
            Color.DARKVIOLET,
            Color.DEEPSKYBLUE,
            Color.DIMGRAY,
            Color.DODGERBLUE,
            Color.FIREBRICK,
            Color.PURPLE,
            Color.RED,
            Color.FORESTGREEN,
            Color.ROYALBLUE,
            Color.SADDLEBROWN,
            Color.SEAGREEN,
            Color.GREEN,
            Color.SIENNA,
            Color.SLATEBLUE,
            Color.INDIANRED,
            Color.SLATEGRAY,
            Color.INDIGO,
            Color.STEELBLUE,
            Color.TEAL
    };

    public static final int INT_255 = 255;


    private static int availableColorIdx = 0;

    private ChartColorStyle() {
    }

    /**
     * Returns the color for a given series; a new color is used
     * in case the series do not have a color yet
     *
     * @param series series
     * @return the color for a given series
     */
    public static Color getColor(XYChart.Series series) {

        // get color we persist for that chart series
        Color seriesColor = CHART_COLOR_MAP.get(series.getName());

        // if null, lookup for the next available color in the color table to assign it to the series
        if (seriesColor == null) {
            seriesColor = AVAILABLE_COLORS[nextAvailableColor()];
            CHART_COLOR_MAP.put(series.getName(), seriesColor);
        }

        return seriesColor;
    }


    private static int nextAvailableColor() {
        availableColorIdx = (availableColorIdx + 1) % AVAILABLE_COLORS.length;
        return availableColorIdx;
    }

    /**
     * Returns the string representation of the color
     *
     * @param color color
     * @return the string representation of the color
     */
    public static String toRGBCode(Color color) {
        Color nColor = (color == null) ? Color.BLACK : color;
        return String.format("#%02X%02X%02X", (int) (nColor.getRed() * INT_255), (int) (nColor.getGreen() * INT_255),
                (int) (nColor.getBlue() * INT_255));
    }

    /**
     * Returns the string representation of the color
     *
     * @param color   color
     * @param opacity opacity
     * @return the string representation of the color
     */
    public static String toRGBCode(Color color, double opacity) {
        return String.format("%s%02X", toRGBCode(color), (int) (opacity * INT_255));
    }

    /**
     * Returns the string representation of the color of the given series
     * @param series series
     * @return the string representation of the color
     */
    public static String getRGBCodeColor(XYChart.Series series) {
        return toRGBCode(getColor(series));
    }

}
