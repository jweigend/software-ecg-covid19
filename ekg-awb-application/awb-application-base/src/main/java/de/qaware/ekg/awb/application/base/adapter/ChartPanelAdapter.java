//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.base.adapter;

import de.qaware.ekg.awb.application.base.view.ViewMapper;
import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.common.ui.view.EkgView;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryContextEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.ChartAnalyticsPanel;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.ChartPanelController;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is a adapter between the Navigator and the chart Panel Controller
 * <p>
 * All Event for the starting or reusing of a chart panel are handled in this adapter
 */

@Alternative
public class ChartPanelAdapter extends ChartPanelController {

    @Inject
    private ChartPanelManager chartPanelManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chartPanelManager.init();
        super.initialize(url, rb);
    }

    /**
     * Handle the {@link QueryContextEvent}.
     * <p/>
     * It updates the search fields of the current chart panel with the data from the event.
     *
     * @param event The information for the new search information
     * @return false due the event is not consumed
     */
    @EkgEventSubscriber(eventClass = QueryContextEvent.class)
    public boolean queryContextEventHandler(QueryContextEvent event) {
        // Only the last active controller reacts on chart updates
        if (chartPanelManager.getLastActiveController() == this) {
            chartPanelManager.updateControllerByEvent(this, event);
            return true;
        }
        return false;
    }

    /**
     * The ChartPanelManager controls the set of {@link ChartPanelController}.
     * <p>
     * It takes care that the panels correct opened, closed and notified.
     */
    @Singleton
    public static class ChartPanelManager {

        @Inject
        private WindowManager windowManager;

        private FXMLView<ChartPanelController> lastActiveView;

        /**
         * Initialize the panels controller.
         */
        public void init() {
            windowManager.focusedViewProperty().addListener((o, ov, activeView) -> updateLastActiveController(activeView));
        }

        /**
         * Update listener for detecting the last active {@link ChartPanelController}.
         *
         * @param activeView The current active view.
         */
        @SuppressWarnings("unchecked")
        private void updateLastActiveController(View activeView) {
            if ((activeView instanceof FXMLView) && (((FXMLView) activeView).getController()) instanceof ChartPanelController) {
                lastActiveView = (FXMLView<ChartPanelController>) activeView;
            }
        }

        /**
         * Get the last active {@link ChartPanelController}.
         *
         * @return The last active {@link ChartPanelController}.
         */
        public ChartPanelController getLastActiveController() {
            return lastActiveView != null ? lastActiveView.getController() : null;
        }

        /**
         * Creates a new chart panel and opens (shows) it.
         */
        public void createNewChartPanel() {
            try {
                EkgView<ChartPanelController> chartView = ChartAnalyticsPanel.createInstance();
                FXMLView<ChartPanelController> view = new ViewMapper<ChartPanelController>().convert(chartView, Position.CENTER, 0);

                windowManager.register(view);
                lastActiveView = view;
            } catch (IOException e) {
                throw new IllegalStateException("Error while opening new chart window.", e);
            }

        }

        /**
         * Check if the given view contains a chart panel.
         *
         * @param view The view to check for a chart panel view.
         * @return true if the given view is a chart panel. otherwise false.
         */
        public boolean isChartPanel(View view) {
            return view.getViewId().startsWith(ChartAnalyticsPanel.VIEW_PREFIX);
        }

        /**
         * Returns true if at least one chart panel is visible.
         *
         * @return True if at least one chart panel is visible, false otherwise.
         */
        public boolean hasChartPanel() {
            for (View view : windowManager.getVisibleViews()) {
                if (isChartPanel(view)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Handles the {@link QueryContextEvent} if a new view should be open.
         * <p>
         * It opens a new view either when {@link QueryContextEvent#enforceNewView()} returns true or when no chart panel is
         * visible.
         *
         * @param event The information about the new search.
         * @return indicates whether new Panel was opened
         */
        @EkgEventSubscriber(eventClass = QueryContextEvent.class)
        public boolean openChartPanel(QueryContextEvent event) {
            if (event.getOpeningMode() == OpeningMode.NEW_VIEW || !hasChartPanel()) {
                createNewChartPanel();
            }
            updateControllerByEvent(lastActiveView.getController(), event);
            return true;
        }


        /**
         * Reloads the Graph with the bookmark. A bookmark saves the state of a graph and can be reactivated with this
         * method. Attention: This method overwrites the old metrics which may not be saved!
         *
         * @param event - the bookmark event which contains all the necessary information
         */
        public void openChartPanelWithBookmark(@Observes BookmarkEvent event) {
            if (event.getOpeningMode() == OpeningMode.NEW_VIEW || !hasChartPanel()) {
                createNewChartPanel();
            }

            getLastActiveController().updateByBookmarkEvent(event);

            windowManager.showView(lastActiveView);
        }

        /**
         * Handle the {@link QueryContextEvent}.
         * <p/>
         * It updates the search fields of the current chart panel with the data from the event.
         *
         * @param controller the controller the event was sent to
         * @param event      event
         */
        public void updateControllerByEvent(ChartPanelController controller, QueryContextEvent event) {
            controller.updateByContextEvent(event);
        }


    }
}
