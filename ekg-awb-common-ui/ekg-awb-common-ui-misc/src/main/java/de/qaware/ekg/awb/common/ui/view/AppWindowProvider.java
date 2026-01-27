package de.qaware.ekg.awb.common.ui.view;

import javafx.stage.Window;

/**
 * A simple helper class that
 */
public class AppWindowProvider {

    private Window appWindow;

    /**
     * Creates a new AppWindowProvider instance that
     * is responsible to serve the Window instance given
     * via constructor parameter.
     *
     * @param appWindow the Window instance that should provided by this class
     */
    public AppWindowProvider(Window appWindow) {
        this.appWindow = appWindow;
    }

    public Window getAppWindow() {
        return appWindow;
    }
}
