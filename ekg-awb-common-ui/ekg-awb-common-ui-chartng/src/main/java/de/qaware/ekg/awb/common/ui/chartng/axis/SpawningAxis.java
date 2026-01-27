package de.qaware.ekg.awb.common.ui.chartng.axis;

import javafx.scene.chart.Axis;

/**
 * The SpawningAxis interface represents JavaFX chart {@link Axis} implementations
 * that are able to create a clone of itself.
 * This can used than a chart should be copied including the axis and all it's states
 * and listeners.
 */
public interface SpawningAxis<T extends Number> extends Bondable {

    /**
     * Returns a clone of this axis instance with
     * all necessary attributes copied except the
     * bounding of min/max.
     *
     * @return the spawned clone of this axis
     */
    SpawningAxis<T> spawn();

    /**
     * Returns a clone of this axis instance with
     * all necessary attributes and bounded connections to others.
     *
     * @return the spawned clone of this axis
     */
    SpawningAxis<T> spawnBounded();

    /**
     * Return the underlying {@link Axis} class this SpawningAxis is derived from.
     * Use this method to eliminate the need for own casting in the caller code.
     *
     * @return this axis instance casted as JavaFX {@link Axis}
     */
    Axis<T> castToAxis();
}
