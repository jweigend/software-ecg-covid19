package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms;

import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * SimplificationService that will remove all points of a series that have an identical value
 * to the previous value. So only the changes of an series will remain.
 */
public class ValueChangeSimplificationService {

    public List<TimeSeries> simplify(List<TimeSeries> timeSeries) {

        for (TimeSeries series : timeSeries) {
            List<Value> values = series.getValues();
            List<Value> resultValues = new ArrayList<>();

            Value lastValue = new Value(0, Double.MIN_VALUE);
            for (Value value : values) {
                if (value.getValue() != lastValue.getValue()) {
                    resultValues.add(value);
                    lastValue = value;
                }
            }

            if (values.get(values.size() - 1).getTimestamp() != lastValue.getTimestamp()) {
                resultValues.add(values.get(values.size() - 1));
            }

            values.clear();
            values.addAll(resultValues);
        }

        return timeSeries;
    }
}
