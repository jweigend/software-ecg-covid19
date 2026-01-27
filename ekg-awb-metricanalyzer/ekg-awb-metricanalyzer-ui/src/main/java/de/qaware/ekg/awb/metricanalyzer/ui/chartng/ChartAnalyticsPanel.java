package de.qaware.ekg.awb.metricanalyzer.ui.chartng;

import de.qaware.ekg.awb.common.ui.view.EkgView;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements the complete chart view component with
 * chart panel itself, the chart legend & actions and the
 * filter/control panel.
 */
public class ChartAnalyticsPanel {

    public static final String VIEW_PREFIX = "ekg:metric:";

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    /**
     * private constructor because this class has to create view
     * {@link ChartAnalyticsPanel#createInstance()} method only.
     */
    private ChartAnalyticsPanel() {
        // no op
    }

    /**
     * Initialize a new chart panel view.
     *
     * @return the new chart panel view.
     * @throws IOException In case of the view can not be initialized.
     */
    public static EkgView<ChartPanelController> createInstance() throws IOException {

        int id = NEXT_ID.getAndIncrement();

        return new EkgView.Builder<ChartPanelController>()
                .withId(VIEW_PREFIX + id)
                .withTitle("Metric (" + id + ")")
                .withPos(EkgView.Position.CENTER)
                .withFile(ChartAnalyticsPanel.class.getResource("ChartPanel.fxml"))
                .withToolTipInfo("The metric viewer")
                .build();
    }
}
