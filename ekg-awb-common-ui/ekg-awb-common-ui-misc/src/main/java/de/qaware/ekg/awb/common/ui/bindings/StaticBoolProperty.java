package de.qaware.ekg.awb.common.ui.bindings;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Continence class that represents an Boolean property that always has the state
 * <code>false</code>
 */
public final class StaticBoolProperty {

    public final static ReadOnlyBooleanProperty TRUE = new SimpleBooleanProperty(true);

    public final static ReadOnlyBooleanProperty FALSE = new SimpleBooleanProperty(false);
}
