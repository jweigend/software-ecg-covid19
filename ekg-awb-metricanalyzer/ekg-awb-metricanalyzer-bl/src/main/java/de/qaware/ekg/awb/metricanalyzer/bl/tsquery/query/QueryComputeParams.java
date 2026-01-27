package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query;

import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingType;

public class QueryComputeParams {

    /**
     * smoothingGranularity that controls if smoothing is enabled and how detailed it works
     */
    private SeriesSmoothingGranularity seriesSmoothingGranularity = SeriesSmoothingGranularity.AUTO;

    /**
     * specifies the smoothing type of the values (min, max, average, median).
     */
    private SeriesSmoothingType seriesSmoothingType = SeriesSmoothingType.NONE;

    /**
     * specifies if the series should be combined and if, that kind of algorithm should use
     */
    private SeriesCombineMode seriesCombineMode = SeriesCombineMode.NONE;

    /*
     * threshold for all points of this chart
     *
     * specifies the maximum amount of points for all series together;
     * 0 indicates, that no simplification should be started
     */
    private int threshold = 10000;


    public SeriesSmoothingGranularity getSeriesSmoothingGranularity() {
        return seriesSmoothingGranularity;
    }

    public void setSeriesSmoothingGranularity(SeriesSmoothingGranularity seriesSmoothingGranularity) {
        this.seriesSmoothingGranularity = seriesSmoothingGranularity;
    }

    public SeriesSmoothingType getSeriesSmoothingType() {
        return seriesSmoothingType;
    }

    public void setSeriesSmoothingType(SeriesSmoothingType seriesSmoothingType) {
        this.seriesSmoothingType = seriesSmoothingType;
    }

    public SeriesCombineMode getSeriesCombineMode() {
        return seriesCombineMode;
    }

    public void setSeriesCombineMode(SeriesCombineMode seriesCombineMode) {
        this.seriesCombineMode = seriesCombineMode;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
