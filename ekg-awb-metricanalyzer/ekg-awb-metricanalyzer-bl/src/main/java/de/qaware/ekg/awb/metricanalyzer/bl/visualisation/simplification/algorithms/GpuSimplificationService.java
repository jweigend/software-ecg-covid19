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
package de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms;

import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.SimplificationService;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

/**
 * Cowl, new GPU based simplifier Service.
 */
@Alternative
@Singleton
public class GpuSimplificationService implements SimplificationService {

    private static final float START_TOLERANCE = 0.01f;
    private static final int MAX_STEPS = 10;

    private static final Logger LOGGER = EkgLogger.get();

    /**
     * Initializes the GPU simplifier.
     */
    @PostConstruct
    public void postConstruct() {
        LOGGER.info("Initializing CPU simplifier.");
    }

    /**
     * Closes the GPU simplifier.
     */
    @PreDestroy
    public void preDestroy() {
        LOGGER.info("Closing CPU simplifier.");
    }

    @Override
    public List<TimeSeries> simplify(List<TimeSeries> timeSeries, int threshold) {
        if (threshold == 0) {
            return timeSeries;
        }

        LOGGER.debug("Simplifying {} counters.", timeSeries.size());

        // Ugly code copied from the old impl
        int singleThreshold = (int) round((double) threshold / timeSeries.size());

        timeSeries.forEach(counter -> {
            List<Value> values = counter.getValues();
            int orgSize = values.size();
            LOGGER.debug("Simplifying {} values.", values.size());

            float tolerance = START_TOLERANCE;

            for (int step = 0; step < MAX_STEPS && values.size() > singleThreshold; ++step, tolerance *= 10) {
                float[] tSeries = new float[values.size()];
                float[] vSeries = new float[values.size()];

                copyValues(values, tSeries, vSeries);
            }
        });

        return timeSeries;
    }

    private static List<Value> filterValues(List<Value> values, boolean[] filter) {
        List<Value> newValues = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); ++i) {
            if (filter[i]) {
                newValues.add(values.get(i));
            }
        }
        return newValues;
    }

    private static void setValues(List<Value> values, List<Value> newValues) {
        values.clear();
        values.addAll(newValues);
    }

    private static void copyValues(List<Value> values, float[] tSeries, float[] vSeries) {
        for (int i = 0; i < values.size(); ++i) {
            Value value = values.get(i);
            tSeries[i] = value.getTimestamp();
            vSeries[i] = (float) value.getValue();
        }
    }


}
