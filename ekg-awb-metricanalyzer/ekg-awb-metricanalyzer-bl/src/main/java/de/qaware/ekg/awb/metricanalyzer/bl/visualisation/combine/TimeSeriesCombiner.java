package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.combine;

import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.List;

public interface TimeSeriesCombiner {

    TimeSeries combine(String newMetricName, List<TimeSeries> timeSeriesList);
}
