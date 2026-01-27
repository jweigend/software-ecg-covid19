package de.qaware.ekg.awb.common.ui.explorer.api;

/**
 * Handler to update the progress for an {@code ItemBuilder}.
 */
public interface ProgressNotifier {

    /**
     * Updates the message property and place it within the progress bar.
     *
     * @param message The progress message.
     */
    void updateMessage(String message);

    /**
     * Update the progress info.
     *
     * @param workDone A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be
     *                 clamped at max. If the value passed is negative, or Infinity, or NaN, then the resulting
     *                 percentDone will be -1 (thus, indeterminate).
     * @param max      A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
     */
    void updateProgress(double workDone, double max);

    /**
     * Update the progress info and place the message within the progress bar.
     *
     * @param message  The progress message.
     * @param workDone A value from Double.MIN_VALUE up to max. If the value is greater than max, then it will be
     *                 clamped at max. If the value passed is negative, or Infinity, or NaN, then the resulting
     *                 percentDone will be -1 (thus, indeterminate).
     * @param max      A value from Double.MIN_VALUE to Double.MAX_VALUE. Infinity and NaN are treated as -1.
     */
    void updateProgress(String message, double workDone, double max);

    /**
     * Update the progress info and place the message within the progress bar.
     *
     * @param message  The progress message.
     * @param workDone A value from Long.MIN_VALUE up to max. If the value is greater than max, then it will be clamped
     *                 at max. If the value passed is negative then the resulting percent done will be -1 (thus,
     *                 indeterminate).
     * @param max      A value from Long.MIN_VALUE to Long.MAX_VALUE.
     */
    default void updateProgress(String message, long workDone, long max) {
        updateProgress(message, (double) workDone, (double) max);
    }
}
