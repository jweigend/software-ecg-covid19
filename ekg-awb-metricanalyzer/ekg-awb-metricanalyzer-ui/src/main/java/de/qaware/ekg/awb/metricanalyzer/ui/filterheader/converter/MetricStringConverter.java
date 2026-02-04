//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import javafx.util.StringConverter;

public class MetricStringConverter extends StringConverter<Metric> {

    private static final String EMPTY_METRIC = "";

    @Override
    public String toString(Metric metric) {
        if (metric != null) {
            return metric.getName();
        }

        return EMPTY_METRIC;
    }

    @Override
    public Metric fromString(String s) {
        if (s != null) {
            return new Metric(s);
        }

        return new Metric(EMPTY_METRIC);
    }
}
