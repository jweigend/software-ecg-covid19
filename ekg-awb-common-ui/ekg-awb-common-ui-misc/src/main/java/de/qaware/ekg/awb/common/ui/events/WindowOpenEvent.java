package de.qaware.ekg.awb.common.ui.events;

/**
 * Extends an Event by the ability to request open a new view when receiving the event.
 */
public interface WindowOpenEvent {
    /**
     * Returns if the event should be handled within a new view independent if a view exists or not.
     * <p>
     * {@code true} will be interpreted as {@link OpeningMode#NEW_VIEW} or {@link OpeningMode#MERGE_VIEW}. {@code
     * false} as {@link OpeningMode#CLEAR_VIEW}.
     *
     * @return true if a new view should be opened. otherwise false.
     */
    boolean enforceNewView();

    /**
     * Get the mode how a item should be shown.
     *
     * @return the opening mode.
     */
    OpeningMode getOpeningMode();
}
