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
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Implementation of simplifier service.
 * This service simplifies a line by checking the
 * change of the incoming gradient to the outgoing gradient of a point
 */
@Alternative
@Singleton
public class LineGradientSimplificationService implements SimplificationService {

    private static final Logger LOGGER = EkgLogger.get();

    /**
     * Initializes the simplifier.
     */
    @PostConstruct
    public void postConstruct() {
        LOGGER.info("Initializing line gradient simplifier.");
    }

    /**
     * Shutdown the simplifier.
     */
    @PreDestroy
    public void preDestroy() {
        LOGGER.info("Closing line gradient simplifier.");
    }


    @Override
    public List<TimeSeries> simplify(List<TimeSeries> timeSeries, int threshold) {

        long beforeSimplifying = timeSeries.stream().mapToLong(c -> c.getValues().size()).sum();

        if (beforeSimplifying > threshold) {

            final double tolerance = LineGradientSimplifier.getTolerance(timeSeries, threshold);

            if (tolerance >= 0.0) {
                timeSeries.parallelStream().forEach(series -> {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new IllegalStateException("Thread interrupted.");
                    }
                    int orgSize = series.getValues().size();
                    List<Value> values = LineGradientSimplifier.simplify(series, tolerance);

                    LOGGER.debug("Line simplified, Tolerance {}: {} reduced to {}",
                            Math.round(tolerance * 100.0) / 100.0, orgSize, values.size());
                });
            }
        }

        return timeSeries;
    }


    /**
     * Simplifies a line by checking the gradient diff to and from a point
     */
    private static final class LineGradientSimplifier {

        public static final int MIN_VALUE_COUNT = 3;
        public static final double NO_SIMPLIFICATION = -1;

        /**
         * Constructor for the LineSimplifier
         */
        private LineGradientSimplifier() {
            // NoOp
        }

        public static double calculateLinearGradient(Coordinate p1, Coordinate p2) {
            double dX = p2.getX() - p1.getX();
            return dX < 0.0 ? Double.MAX_VALUE : (p2.getY() - p1.getY()) / (dX);
        }


        /**
         * Returns the tolerance for the difference of incoming and
         * outgoing gradient accepted for the threshold
         *
         * @param counters list of point lists
         * @param threshold threshold
         * @return tolerance
         */
        public static double getTolerance(List<TimeSeries> counters, int threshold) {

            // No Simplification
            if (threshold == 0) {
                return NO_SIMPLIFICATION;
            }

            List<Double> diffGradient = Collections.synchronizedList(new ArrayList<>());

            counters.parallelStream().forEach(counter -> {
                if (Thread.currentThread().isInterrupted()) {
                    throw new IllegalStateException("Thread interrupted.");
                }

                List<Value> values = counter.getValues();

                Deque<Integer> noGradient = new LinkedList<>();

                iterateValues(values, (idx, m) -> {
                            double diff = Math.abs(m[1] - m[0]);
                            if (diff > 0.0) {
                                //  synchronized
                                diffGradient.add(diff);
                            } else {
                                // synchronized
                                noGradient.addFirst(idx);
                            }
                        }
                );

                // Threshold is not off
                noGradient.forEach(idx -> values.remove((int) idx));
            });

            if (diffGradient.size() > threshold) {
                Collections.sort(diffGradient);
                return diffGradient.get(diffGradient.size() - threshold);
            } else {
                // already simplified 0.0 Gradient Diff -> no simplification necessary
                return NO_SIMPLIFICATION;
            }
        }

        private static void iterateValues(List<Value> values, BiConsumer<Integer, double[]> consumer) {

            if (values.size() > MIN_VALUE_COUNT) {
                final Coordinate[] pX = {null,
                        new Coordinate(values.get(0).getTimestamp(), values.get(0).getValue()),
                        new Coordinate(values.get(1).getTimestamp(), values.get(1).getValue())};
                final double[] m = {0.0, calculateLinearGradient(pX[1], pX[2])};

                IntStream.range(1, values.size() - 1)
                        .forEach(idx -> {
                            pX[0] = pX[1];
                            pX[1] = pX[2];
                            pX[2] = new Coordinate(values.get(idx + 1).getTimestamp(),
                                    values.get(idx + 1).getValue());

                            m[0] = m[1];
                            m[1] = calculateLinearGradient(pX[1], pX[2]);

                            consumer.accept(idx, m);

                        });
            }
        }

        /**
         * Simplifies an Array of Coordinates with the given tolerance.
         * All vertices in the simplified linestring will be within this
         * distance of the original linestring.
         *
         * @param series   counter with array of points in value
         * @param tolerance the tolerance to accept a point
         * @return an array of Coordinates
         */
        public static List<Value> simplify(TimeSeries series, double tolerance) {
            List<Value> values = series.getValues();

            Deque<Integer> noGradient = new LinkedList<>();

            iterateValues(values, (idx, m) -> {
                        double diff = Math.abs(m[1] - m[0]);
                        if (diff <= tolerance) {
                            noGradient.addFirst(idx);
                        }
                    }
            );

            noGradient.forEach(idx -> values.remove((int) idx));

            return values;
        }
    }
}
