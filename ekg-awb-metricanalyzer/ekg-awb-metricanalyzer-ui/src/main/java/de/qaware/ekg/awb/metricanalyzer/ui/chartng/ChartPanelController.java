package de.qaware.ekg.awb.metricanalyzer.ui.chartng;

import de.qaware.ekg.awb.common.ui.chartng.ChartType;
import de.qaware.ekg.awb.common.ui.chartng.ColoredChart;
import de.qaware.ekg.awb.common.ui.chartng.ZoomableStackedChart;
import de.qaware.ekg.awb.common.ui.chartng.axis.DateAxis;
import de.qaware.ekg.awb.common.ui.chartng.axis.MetricAxis;
import de.qaware.ekg.awb.common.ui.chartng.legend.LegendActionCallbacks;
import de.qaware.ekg.awb.common.ui.chartng.legend.StackedChartLegend;
import de.qaware.ekg.awb.common.ui.chartng.zoom.Zoomable;
import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingType;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.MetricBookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryContextEvent;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.CreateBookmarkDialog;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.command.*;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.export.ExcelExportSeriesData;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.export.ExcelExporter;
import de.qaware.ekg.awb.metricanalyzer.ui.chartng.export.ScreenshotExportDialog;
import de.qaware.ekg.awb.metricanalyzer.ui.filterheader.ChartHeaderController;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Controller that manages the whole MetricAnalyser analytics pane that contains the
 * EKG chart as primary component and filter header, chart legend and panel with
 * additional actions.
 *
 * This controller knows as single point of knowledge how to wire all these components
 * correctly and also handles command of Bookmarks scripts using a dedicate CommandProcessor.
 */
@SuppressWarnings("unused")
public class ChartPanelController implements Initializable {

    private static final String AXIS_STYLESHEET = "/de/qaware/ekg/awb/metricanalyzer/ui/chartng/ChartPanelStyle.css";

    private static final Logger LOGGER = EkgLogger.get();

    @FXML
    private AnchorPane overlayPane;

    @FXML
    private BorderPane chartContainer;

    @FXML
    private Pane chartActionsPanel;

    @FXML
    private StackedChartLegend chartLegend;

    @FXML
    private ChartHeaderController chartHeaderController;

    @FXML
    private CheckBox cbAlignYAxis;

    @FXML
    private CheckBox cbShowGridLines;

    @FXML
    private Button btnExportToExcel;

    @FXML
    private Button btnExportToPNG;

    @FXML
    private ComboBox<ChartTimeZone> cbTimeZone;


    private ChartCommandProcessor commandProcessor = EkgLookup.lookup(ChartCommandProcessor.class);

    private ZoomableStackedChart zoomableStackedChart;

    private final ChartPanelModel viewModel = new ChartPanelModel();

    /**
     * Event Bus
     */
    private final EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    //================================================================================================================
    //  Initializable interface implementation
    //================================================================================================================

    /* (non-Javadoc)
     * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // set all members to initial values
        initializeViewState();

        // init pseudo classes and other view styles that
        // will setup programmatically
        initializeViewStyles();

        // init default behaviour (validation, filter, ...0) and binding
        // before calling methods overwritten by deriving classes
        initializeViewBehavior();

        // update view from model properties and link the binding if exists
        initializeModelToView();

        // init event handlers
        initializeHandler();
    }


    //=================================================================================================================
    // controller API
    //=================================================================================================================

    /**
     * Handle the delegation of a {@link QueryContextEvent}.
     *
     * It updates the search fields of the current chart panel with the data from the event
     * and load the according metric data to the chart panel.
     *
     * @param event the event with filters, EkgRepository to use and more info need to setup the analytics panel
     */
    public void updateByContextEvent(QueryContextEvent event) {

        if (event.getOpeningMode() == OpeningMode.CLEAR_VIEW) {
            commandProcessor.executeCommand(ChartCommand.CLEAR_ALL);
        } else {
            commandProcessor.executeStringArgCommand(ChartCommand.PUSH_BASE_TO_BG, null, UUID.randomUUID().toString());
        }

        chartHeaderController.setRepository(event.getRepository());
        chartHeaderController.setQueryContext(event.getFilterParams(), event.getOpeningMode());

        viewModel.setEkgRepository(event.getRepository());
        viewModel.setBaseChartFilterParams(event.getFilterParams());

        reloadGraphs(true);
    }

    /**
     * Handle the delegation of a {@link BookmarkEvent} by
     * using it's data to update the chart view.
     *
     * @param event the bookmark event to proceed
     */
    public void updateByBookmarkEvent(BookmarkEvent event) {

        if (!(event.getBookmark() instanceof MetricBookmark)) {
            return;
        }

        zoomableStackedChart.getController().clearAll();

        MetricBookmark metricBookmark = (MetricBookmark) event.getBookmark();

        chartHeaderController.setRepository(event.getSourceRepository());
        chartHeaderController.setQueryContext(new QueryFilterParams(), event.getOpeningMode());
        viewModel.setEkgRepository(event.getSourceRepository());


        CommandProtocol protocol = CommandProtocol.createFromString(metricBookmark.getSerializedCommandProtocol());

        // delegate the whole command protocol to execute. The specified callback will set the
        // filters/context parameters of the filter header
        commandProcessor.executeProtocol(protocol, () -> {
            String fetchParametersAsString = protocol.getLastSerializedChartLoadParameter();

            if (StringUtils.isBlank(fetchParametersAsString)) {
                LOGGER.error("there is no serialized ChartDataFetchParameters available in command protocol " +
                        "served by the bookmark. This is unexpected.");
                return;
            }

            ChartDataFetchParameters fetchParameter = CommandProtocolManager.deserializeChartFetchParams(fetchParametersAsString);

            // set context / filters to the analytic workbench
            Platform.runLater(() -> {
                viewModel.suppressEventHandling(true); // we need this otherwise various service calls will follow

                viewModel.setBaseChartFilterParams(fetchParameter.getQueryFilterParams());
                viewModel.setBaseChartComputeParams(fetchParameter.getQueryComputeParams());

                chartHeaderController.setQueryContext(fetchParameter.getQueryFilterParams(), OpeningMode.UPDATE);
                chartHeaderController.setComputeParameters(fetchParameter.getQueryComputeParams());

                viewModel.suppressEventHandling(false);
            });
        });
    }

    //=================================================================================================================
    // controller default initializer
    //=================================================================================================================

    private void initializeViewState() {

        // init axis of default chart
        DateAxis xAxis = new DateAxis();

        MetricAxis yAxis = new MetricAxis();

        yAxis.setLabel(chartHeaderController.getDefinedFilterParameters().getRawQuery());

        // wire this controller and the one of the filter header panel using an own
        // ChartHeaderController.ActionCallbacks implementation that will set to the foreign controller
        chartHeaderController.initButtonHandler(new HeaderButtonActions());

        // create the default line chart and add it to the view container
        ObservableList<XYChart.Series<Long, Double>> emptySeries = FXCollections.observableArrayList();
        ColoredChart baseChart = new ColoredChart(new LineChart<>(xAxis, yAxis, emptySeries));
        baseChart.setId("baseChart");
        chartLegend.setBaseChart(baseChart);
        chartLegend.setActionHandler(new ChartLegendActions());
        zoomableStackedChart = new ZoomableStackedChart(baseChart, chartLegend);
        chartContainer.setCenter(zoomableStackedChart);

        overlayPane.getChildren().add(zoomableStackedChart.getMouseOverlays());

        cbTimeZone.setItems(FXCollections.observableArrayList(ChartTimeZone.getChartTimeZones()));
        cbTimeZone.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> xAxis.setTimeZone(newValue.getTimeZone())
        );
        cbTimeZone.setValue(ChartTimeZone.getDefaultTimeZone());
    }



    private void initializeHandler() {
        commandProcessor.setStackedChart(zoomableStackedChart);
        commandProcessor.setChartDataService(new ChartDataService() {

            /**
             * a boolean flag that indicates if the chart axis should reset on
             */
            private boolean resetChartAxis = true;


            @Override
            protected Task createTask() {
                return new FillChartDataTask(
                        getFilterParams(),
                        getComputeParams(),
                        doResetChartAxis(),
                        viewModel.getEkgRepository(),
                        zoomableStackedChart
                );
            }
        });

        // bind actions
        chartHeaderController.initButtonHandler(new HeaderButtonActions());
        btnExportToExcel.setOnAction(event -> {
            exportChartToExcelSheet();
            chartActionsPanel.requestFocus();
        });

        btnExportToPNG.setOnAction(event -> {
            exportChartToPngFile();
            chartActionsPanel.requestFocus();
        });

        zoomableStackedChart.setOnZoomChanged(this::zoomChangedHandler);

        chartAsDialog(zoomableStackedChart);
    }

    private void initializeModelToView() {

    }


    private void initializeViewBehavior() {
        cbShowGridLines.selectedProperty().addListener((observable, old, isSelected) -> {
            zoomableStackedChart.getBaseChart().toXYChart().setHorizontalGridLinesVisible(isSelected);
            zoomableStackedChart.getBaseChart().toXYChart().setVerticalGridLinesVisible(isSelected);
            chartActionsPanel.requestFocus();
        });

        AtomicBoolean blockAlignHandling = new AtomicBoolean(false);

        zoomableStackedChart.getController().yAxisAlignmentActiveProperty().addListener((observable, oldValue, newValue) -> {
            blockAlignHandling.set(true);
            cbAlignYAxis.setSelected(newValue);
            chartActionsPanel.requestFocus();
            blockAlignHandling.set(false);
        });

        cbAlignYAxis.selectedProperty().addListener((observable, old, isSelected) -> {

            if (blockAlignHandling.get()) {
                return;
            }

            if (isSelected) {
                commandProcessor.executeCommand(ChartCommand.FORCE_ALIGN_Y_AXIS);
            } else {
                commandProcessor.executeCommand(ChartCommand.FREE_ALIGN_Y_AXIS);
            }
            chartActionsPanel.requestFocus();
        });
    }

    private void initializeViewStyles() {
        cbTimeZone.setSkin(new TimeZoneBoxSkin(cbTimeZone));
    }

    //=================================================================================================================
    // private helpers to manage the UI components and it's states
    //=================================================================================================================

    /**
     * An handler that handles the case where the user zoom in into the chart or vice versa.
     * It will update the model with the start/end date bounds the derived from the zoom rectangle
     * an reloads the base chart series on that base.
     *
     * @param zoomChangedEvent an Zoomable.ZoomEvent instance fired than the user zooms into the chart
     */
    private void zoomChangedHandler(Zoomable.ZoomEvent zoomChangedEvent) {

        // nothing to load if base chart is currently empty
        if (zoomableStackedChart.getBaseChart().isEmpty()) {
            return;
        }

        long diff = (long) zoomChangedEvent.getXAxisUpperBound() - (long) zoomChangedEvent.getXAxisLowerBound();
        QueryFilterParams queryFilterParams = viewModel.getBaseChartFilterParams();

        queryFilterParams.setStart(zoomChangedEvent.getXAxisLowerBound() - diff / 8d);
        queryFilterParams.setEndAsDouble(zoomChangedEvent.getXAxisUpperBound() + diff / 8d);
        reloadGraphs(!zoomChangedEvent.isZoomIn());
        zoomChangedEvent.consume();
    }

    /**
     * Reloads the base chart by fetching the latest time series data from repository using the current
     * defined filter and post-compute parameter set.
     *
     * Existing chart data will be overwritten.
     *
     * @param resetChartAxis boolean flag that control if the chart axis and it's bounds should be reset or not
     */
    private void reloadGraphs(boolean resetChartAxis) {

        viewModel.setBaseChartFilterParams(chartHeaderController.getDefinedFilterParameters());
        viewModel.setBaseChartComputeParams(chartHeaderController.getDefinedComputedParameters());

        commandProcessor.executeObjectArgCommand(
                ChartCommand.LOAD_BASE_CHART_DATA,
                zoomableStackedChart.getBaseChart().getId(),
                new ChartDataFetchParameters(
                        viewModel.getBaseChartFilterParams(),
                        viewModel.getBaseChartComputeParams(),
                        resetChartAxis
                ),
                false
        );
    }

    /**
     * Wraps the SplitPane with Chart and legend in a dialog that will displayed in
     * a separate modal than the user double clicks on the SplitPane.
     *
      * @param stackedChart a ZoomableStackedChart instance that will be wrapped and displayed by a JavaFX dialog
     */
    private void chartAsDialog(ZoomableStackedChart stackedChart) {

        final Dialog chartPaneDialog = new Dialog();

        chartPaneDialog.initModality(Modality.NONE);
        chartPaneDialog.setTitle("Metric");
        chartPaneDialog.getDialogPane().setStyle(
                "-fx-background-color: #FDFDFC; " +
                "-fx-border-color: #0096c9; " +
                "-fx-border-width: 2 0 2 0;");
        chartPaneDialog.setResizable(true);
        chartPaneDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = chartPaneDialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.managedProperty().bind(closeButton.visibleProperty());
        closeButton.setVisible(false);
        chartPaneDialog.getDialogPane().setPadding(new Insets(0, 0, -20, 0));

        stackedChart.setOnMouseClicked(mouseEvent -> {

            // if main button double click
            if (!(mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)) {
                return;
            }

            final Bounds boundsInScreen = chartContainer.getBoundsInLocal();

            // reallocate the chart pane with overlay to dialog and fill the old location with an empty pane
            chartContainer.setCenter(new Pane());
            chartContainer.layout();
            chartPaneDialog.getDialogPane().setContent(zoomableStackedChart);

            // layout the dialog with borrowed chart in the JavaFX thread
            Platform.runLater(() -> {
                chartPaneDialog.setY(boundsInScreen.getMinY());
                chartPaneDialog.setX(boundsInScreen.getMinX());
                chartPaneDialog.setHeight(boundsInScreen.getHeight() * 1.5);
                chartPaneDialog.setWidth(boundsInScreen.getWidth());
                chartPaneDialog.getDialogPane().layout();
            });

            // than dialog will closed by the user the overlay will set back to the primary chart pane
            chartPaneDialog.setOnHidden(windowEvent -> {
                chartPaneDialog.getDialogPane().setContent(null);
                chartContainer.setCenter(zoomableStackedChart);

                Platform.runLater(() -> chartContainer.layout());
            });

            chartPaneDialog.show();
        });
    }

    /**
     * Creates a screenshot of the StackableChart with it's current displayed series
     * and write it as image file to the file system.
     */
    private void exportChartToPngFile() {
        ScreenshotExportDialog screenshotExportDialog = new ScreenshotExportDialog(zoomableStackedChart);
        screenshotExportDialog.showAndWait();
    }

    /**
     * Exports the current graph data from (base) chart to an excel file the user can
     * specify via FileChooser.
     * The export will executed with blocking behavior, the UI didn't respond during that.
     */
    private void exportChartToExcelSheet() {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Specify file for Excel export");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel files", "*.xlsx"),
                new FileChooser.ExtensionFilter("All files", "*")
        );

        File chosenFile = chooser.showSaveDialog(null);

        if (chosenFile == null) {
            return;
        }

        List<ExcelExportSeriesData> seriesData = zoomableStackedChart
                .getBaseChart()
                .getAllSeries()
                .stream()
                .map(series -> new ExcelExportSeriesData(series.getName(), series.getData()))
                .collect(Collectors.toList());

        seriesData.addAll(
                zoomableStackedChart
                .getBackgroundCharts()
                .stream()
                .flatMap(chart -> chart.getAllSeries().stream())
                .map(series -> new ExcelExportSeriesData(series.getName(), series.getData()))
                .collect(Collectors.toList())
        );

        ExcelExporter exporter = new ExcelExporter(chosenFile);
        exporter.createExcelSheetFromSeriesData(seriesData);
    }

    //=================================================================================================================
    // chart legend / chart actions function handler
    //=================================================================================================================

    /**
     * Handler for the chart filter header panel
     */
    private class ChartLegendActions extends LegendActionCallbacks {

        @Override
        public void combineBgChartsRelativeToBase(boolean interpolate) {
            commandProcessor.executeObjectArgCommand(ChartCommand.RELATIVE_COMBINE_TO_BASE, null, interpolate);
        }

        @Override
        public void combineBgAbsolute() {
            commandProcessor.executeCommand(ChartCommand.ABSOLUTE_COMBINE_TO_BASE);
        }

        @Override
        public void pushToBackground(String newChartId) {
            commandProcessor.executeObjectArgCommand(ChartCommand.PUSH_BASE_TO_BG, null, newChartId);
        }

        @Override
        public void clearAll() {
            commandProcessor.executeCommand(ChartCommand.CLEAR_ALL);
        }

        @Override
        public void deleteAllBgCharts() {
            commandProcessor.executeCommand(ChartCommand.DELETE_ALL_BG_CHARTS);
        }

        @Override
        public void deleteBgChart(String chartId) {
            commandProcessor.executeCommand(ChartCommand.DELETE_BG_CHART, chartId);
        }

        @Override
        public void changeChartType(String chartId, ChartType newChartType) {
            commandProcessor.executeObjectArgCommand(ChartCommand.CHANGE_CHART_TYPE, chartId, newChartType);
        }

        @Override
        public void setChartVisible(String chartId, boolean isVisible) {
            commandProcessor.executeObjectArgCommand(ChartCommand.SET_CHART_VISIBLE, chartId, isVisible);
        }

        @Override
        public void changeChartColor(String chartId, Color newColor) {
            commandProcessor.executeObjectArgCommand(ChartCommand.CHANGE_CHART_COLOR, chartId, newColor);
        }

        @Override
        public void changeSeriesColor(String chartId, String seriesName, Color newColor) {
            commandProcessor.executeObjectArgCommand(ChartCommand.CHANGE_SERIES_COLOR, chartId, seriesName, newColor);
        }
    }

    //=================================================================================================================
    // Chart header panel actions (handler callbacks)
    //=================================================================================================================

    /**
     * Handler for the chart filter header panel
     */
    private class HeaderButtonActions extends ChartHeaderController.ActionCallbacks {

        /**
         * Creates the Metrics for the Graph.
         */
        @Override
        public void updateMetricsGraph() {
            reloadGraphs(true);
        }

        /**
         * Creates a new bookmark. A Dialog is shown, in this dialog the user can choose the bookmark's name.
         */
        @Override
        public void createBookmark() {

            MetricBookmark.Builder bookmarkBuilder = new MetricBookmark.Builder()
                    .withCommandProtocol(commandProcessor.getCommandProtocol().toSerializedString())
                    .withProjectName(viewModel.getBaseChartFilterParams().getProjectName());


            CreateBookmarkDialog<MetricBookmark> dialog = new CreateBookmarkDialog<>(bookmarkBuilder, viewModel.getEkgRepository());
            dialog.initOwner(chartHeaderController.getOwnerWindow());

            dialog.showAndWait().ifPresent(bookmarkResultContainer -> {
                MetricsBookmarkService service = viewModel.getEkgRepository().getBoundedService(MetricsBookmarkService.class);

                BookmarkGroup bookmarkGroup = bookmarkResultContainer.getBookmarkGroup();

                // special case: user creates a new BookmarkGroup. In this case we need to persist it
                // in addition to the bookmark itself
                if (bookmarkGroup.isNew() && !bookmarkGroup.isEmptyGroup()) {
                    bookmarkGroup.setBookmarkGroupId(UUID.randomUUID().toString());
                    service.persistNewBookmarkGroup(bookmarkGroup);
                    bookmarkResultContainer.getBookmark().setBookmarkGroupId(bookmarkGroup.getBookmarkGroupId());
                }

                service.persistNewBookmark(bookmarkResultContainer.getBookmark());
                eventBus.publish(new ExplorerUpdateEvent(this, viewModel.getEkgRepository()));
            });
        }

        @Override
        public void changeSmoothingGranularity(SeriesSmoothingGranularity smoothingGranularity) {
            viewModel.getBaseChartComputeParams().setSeriesSmoothingGranularity(smoothingGranularity);
            if (!viewModel.isSuppressEventHandling()) {
                reloadGraphs(true);
            }
        }

        @Override
        public void changeSmoothingType(SeriesSmoothingType aggregation) {
            viewModel.getBaseChartComputeParams().setSeriesSmoothingType(aggregation);
            if (!viewModel.isSuppressEventHandling()) {
                reloadGraphs(true);
            }
        }

        @Override
        public void changeSeriesCombineMode(SeriesCombineMode combineMode) {
            viewModel.getBaseChartComputeParams().setSeriesCombineMode(combineMode);
            if (!viewModel.isSuppressEventHandling()) {
                reloadGraphs(true);
            }
        }

        @Override
        public void changeThreshold(int threshold) {
            viewModel.getBaseChartComputeParams().setThreshold(threshold);

            if (!viewModel.isSuppressEventHandling()) {
                reloadGraphs(true);
            }
        }
    }

    private static class TimeZoneBoxSkin extends ComboBoxListViewSkin<ChartTimeZone> {

        /**
         * Creates a new ComboBoxListViewSkin instance, installing the necessary child
         * nodes into the Control {@link Control::getChildren() children} list, as
         * well as the necessary input mappings for handling key, mouse, etc events.
         *
         * @param timeSeriesCombobox The control that this skin should be installed onto.
         */
        public TimeZoneBoxSkin(ComboBox<ChartTimeZone> timeSeriesCombobox) {
            super(timeSeriesCombobox);
            timeSeriesCombobox.getStyleClass().add("timezoneComboBox");
            timeSeriesCombobox.getStylesheets().add(ChartPanelController.class
                    .getResource("ChartPanelStyle.css").toExternalForm());


            timeSeriesCombobox.setCellFactory(value -> new TimeZoneListCell());
        }

        /**
         * This method should return a Node that will be positioned within the
         * ComboBox 'button' area.
         * @return the node that will be positioned within the ComboBox 'button' area
         */
        public Node getDisplayNode() {
            //noinspection unchecked
            return new ButtonCell((ChartTimeZone)((ComboBox) getNode()).getSelectionModel().getSelectedItem());
        }


        private static class ButtonCell extends Cell<ChartTimeZone> {

            public ButtonCell(ChartTimeZone initialValue) {
                super.getStyleClass().add("timezoneComboBoxBtn");
                this.updateItem(initialValue, false);
            }

            /** {@inheritDoc} */
            @Override
            protected void updateItem(ChartTimeZone selectedValue, boolean empty) {
                super.updateItem(selectedValue, empty);
                setText("");
                setGraphic(null);
            }

            /** {@inheritDoc} */
            @Override protected Skin<?> createDefaultSkin() {
                return new CellSkinBase<>(this);
            }
        }

        private static class TimeZoneListCell extends ListCell<ChartTimeZone> {
            /** {@inheritDoc} */
            @Override
            protected void updateItem(ChartTimeZone timeZone, boolean empty) {
                super.updateItem(timeZone, empty);
                if (timeZone != null) {
                    setText(" " + timeZone.getAlias());
                }
            }
        }
    }
}