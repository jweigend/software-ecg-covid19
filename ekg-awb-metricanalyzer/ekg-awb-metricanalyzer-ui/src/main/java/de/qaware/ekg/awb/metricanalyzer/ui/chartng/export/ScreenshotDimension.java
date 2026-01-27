package de.qaware.ekg.awb.metricanalyzer.ui.chartng.export;

import de.qaware.ekg.awb.sdk.core.NamedEnum;

/**
 * An enumeration that represents the different presets of
 * image export dimensions and it's concrete properties.
 */
public enum ScreenshotDimension implements NamedEnum {

    /**
     * Export the chart in exact the size and dimension ratio as
     * rendered in the application.
     */
    DIM_1_TO_1     ("Original size/dimension ratio",  -1, -1, 1.0),

    /**
     * Export the chart with the same dimension ratio as
     * rendered in the application but with a scale of factor 2.
     */
    DIM_2_TO_1     ("Double original size",          -1, -1, 2.0),

    /**
     * Export the the chart with on of the various standard screen sizes
     * and it's ratio / pixel size.
     */
    DIM_1920_X_1080("Full-HD (1920x1080 | 16∶9)",  1920, 1080, -1.0),
    DIM_1600_X_1200("UXGA    (1600x1200 | 4:3)",   1600, 1200, -1.0),
    DIM_1920_X_1200("WUXGA   (1920x1200 | 16∶10)", 1920, 1200, -1.0),
    DIM_2560_X_1440("WQHD    (2560×1440 | 16∶9)",  2560, 1440, -1.0),
    DIM_3440_X_1440("QHD+    (3440×1440 | 21∶9)",  3440, 1440, -1.0),
    DIM_3840_X_2160("UHD 4K  (3840x2160 | 16∶9)",  3840, 2160, -1.0),

    /**
     * Export the the chart with special super-wide screen / landscape
     * dimension.
     */
    DIM_2000_X_0625("Wide 1  (2000x625  | 16∶5)",  2000, 625,  -1.0),
    DIM_4000_X_1250("Wide 2  (4000x1250 | 16∶5)",  4000, 1250, -1.0);

    //---------------------------------------------------------------------------------------------------------

    /**
     * The alias that represents the dimension preset
     */
    private String name;

    /**
     * the width in pixels of the output image
     * Negative if 1:1 ratio to the captured chart.
     */
    private double width;

    /**
     * the height in pixels of the output image
     */
    private double height;

    /**
     * the scale factor that can defined instead the pixel dimensions
     * to specify the scaling compared to the original chart rendered in the AWB UI.
     * Negative if concrete pixel dimensions specified.
     */
    private double scaleFactor;

    /**
     * Constructs a new instance of ScreenshotDimension with the specified
     * dimension settings.
     *
     * @param name the alias that represents the dimension preset
     * @param width the width in pixels of the output image
     * @param height the height in pixels of the output image
     * @param scaleFactor the scale factor that can defined instead the pixel dimensions
     */
    ScreenshotDimension(String name, double width, double height, double scaleFactor) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the width in pixels of the output image
     *
     * @return the width in pixels
     */
    public double getDimensionWidth() {
        return width;
    }

    /**
     * Returns the height in pixels of the output image
     *
     * @return the height in pixels
     */
    public double getDimensionHeight() {
        return height;
    }

    /**
     * Returns the scale factor compared to the size of the
     * chart in the AWB UI.
     *
     * @return the scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }
}
