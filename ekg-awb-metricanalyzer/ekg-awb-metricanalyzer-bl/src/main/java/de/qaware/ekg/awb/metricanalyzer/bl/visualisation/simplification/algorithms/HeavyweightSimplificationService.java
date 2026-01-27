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
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight.Coordinate;
import de.qaware.ekg.awb.metricanalyzer.bl.visualisation.simplification.algorithms.heavyweight.LineSimplifier;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.List;

import static java.lang.Math.round;

/**
 * Old, heavyweight simplifier service impl.
 */
@Alternative
@Singleton
public class HeavyweightSimplificationService implements SimplificationService {

    private static final Logger LOGGER = EkgLogger.get();

    /**
     * Initializes the simplifier.
     */
    @PostConstruct
    public void postConstruct() {
        LOGGER.info("Initializing heavyweight simplifier.");
    }

    /**
     * Shutdown the simplifier.
     */
    @PreDestroy
    public void preDestroy() {
        LOGGER.info("Closing heavyweight simplifier.");
    }

    @Override
    public List<TimeSeries> simplify(List<TimeSeries> timeSeries, int threshold) {
        // Ugly code copied from the old impl
        if (threshold == 0) {
            return timeSeries;
        }

        int singleThreshold = (int) round((double) threshold / timeSeries.size());
        timeSeries.parallelStream().forEach(counter -> {
            Coordinate[] newCoordinates;
            double tolerance = 0.01;
            int step = 1;
            do {
                if (Thread.currentThread().isInterrupted()) {
                    throw new IllegalStateException("Thread interrupted.");
                }
                List<Value> values = counter.getValues();
                int orgSize = values.size();
                if (orgSize < singleThreshold) {
                    break; // no simplification
                }
                Coordinate[] coordinates = new Coordinate[values.size()];
                int i = 0;
                for (Value value : values) {
                    coordinates[i++] = new Coordinate(value.getTimestamp(), value.getValue());
                }

                values.clear();

                newCoordinates = LineSimplifier.simplify(coordinates, tolerance);
                for (Coordinate newCoordinate : newCoordinates) {
                    values.add(new Value((long) newCoordinate.getX(), newCoordinate.getY()));
                }

                LOGGER.debug("Line simplified, Step {}: {} reduced to {}", step, orgSize, values.size());
                tolerance *= 10;
                step += 1;
            } while (newCoordinates.length > singleThreshold);
        });

        return timeSeries;
    }

}
