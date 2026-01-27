//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader;

import afester.javafx.svg.SvgLoader;
import de.qaware.ekg.awb.common.ui.bindings.Bindings;
import de.qaware.ekg.awb.common.ui.components.FilterableComboBox;
import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesCombineMode;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingGranularity;
import de.qaware.ekg.awb.metricanalyzer.bl.api.SeriesSmoothingType;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Container;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryComputeParams;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter.InstantToUtcDateConverter;
import de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter.NamedEnumConverter;
import de.qaware.ekg.awb.metricanalyzer.ui.filterheader.model.ChartHeaderBusinessProcess;
import de.qaware.ekg.awb.metricanalyzer.ui.filterheader.model.ChartHeaderModel;
import de.qaware.ekg.awb.project.api.ProjectViewFlavorChangedEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectFlavor;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriber;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableList;

/**
 * Controller class that manages the filter header panel that is on top of the
 * time series chart and provides multiple facet filters and query/compute settings.
 */
public class ChartHeaderController implements Initializable {

    private static final int NUMBER_DUMMY_LOADING_ITEMS = 4;

    private static final String LOADING_TEXT = "Loading...";

    /**
     * view model for the chart header
     */
    private final ChartHeaderBusinessProcess viewModel = new ChartHeaderBusinessProcess();

    /**
     * Default callback for handling actions
     */
    private ActionCallbacks actions = new ActionCallbacks();

    /* ------------------ generic filter (I) ------------------ */

    @FXML
    private GridPane measurementFilterPane;

    @FXML
    private Label lbMeasurement;

    @FXML
    private FilterableComboBox<Measurement> cbxMeasurement;

    /* ------------------ classic filter ---------------------- */

    @FXML
    private Label lbHostGroup;

    @FXML
    private FilterableComboBox<HostGroup> cbxHostGroup;

    @FXML
    private Label lbHost;

    @FXML
    private FilterableComboBox<Host> cbxHost;

    /* ------------------- cloud filter ----------------------- */

    @FXML
    private Label lbNameSpace;

    @FXML
    private FilterableComboBox<Namespace> cbxNamespace;

    @FXML
    private GridPane serviceFilterPane;

    @FXML
    private Label lbService;

    @FXML
    private FilterableComboBox<Service> cbxService;

    @FXML
    private Label lbPod;

    @FXML
    private FilterableComboBox<Pod> cbxPod;

    @FXML
    private Label lbContainer;

    @FXML
    private FilterableComboBox<Container> cbxContainer;

    /* ------------------ generic filter (II) ----------------- */

    @FXML
    private Label lbProcess;

    @FXML
    private FilterableComboBox<Process> cbxProcess;

    @FXML
    private Label lbMetricGroup;

    @FXML
    private FilterableComboBox<MetricGroup> cbxMetricGroup;

    /* ------------------ generic filter (III) ----------------- */

    @FXML
    private Label lbMetric;

    @FXML
    private FilterableComboBox<Metric> cbxMetric;

    @FXML
    private Label labelExclude;

    @FXML
    private TextField txtExclude;

    @FXML
    private Label labelExpertQuery;

    @FXML
    private TextArea taExpertQuery;

    @FXML
    private DatePicker startPicker;

    @FXML
    private DatePicker stopPicker;

    /* ------------------ other panel components ----------------- */

    @FXML
    private ComboBox<SeriesSmoothingType> cbxSmoothingType;

    @FXML
    private ComboBox<SeriesSmoothingGranularity> cbxSmoothingGranularity;

    @FXML
    private ComboBox<Integer> cbxThreshold;

    @FXML
    private Label lbEnableAllFilter;

    @FXML
    private CheckBox cbEnableAllFilter;

    @FXML
    private Button btRefreshGraph;

    @FXML
    private Button btBookmark;

    @FXML
    private ComboBox<SeriesCombineMode> cbxCombineMode;

    @FXML
    private VBox seriesFilterGroup;

    /* ------------------ misc vars ----------------- */

    private HashSet<Object> updatedComboboxes = new HashSet<>();


    /* ------------------------------------ Initializable Interface implementation ---------------------------------- */

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initViewStyles();
        initComboBoxCellFactories();
        configureClassicOrCloudFilterAccessibility();
        configureExtendedFilterAccessibility();
        bindFilterToModel();
        initHandler();
        initDefaultState();
    }

    //------------------------------------------------------------------------------------------------------------------
    // controller api used by other EKG components
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Initialize the Button handler
     * @param actions actions
     */
    public void initButtonHandler(ActionCallbacks actions) {
        this.actions = actions;
        btBookmark.setOnAction(e -> actions.createBookmark());
        btRefreshGraph.setOnAction(e -> actions.updateMetricsGraph());
    }

    /**
     * Initialize the default states of the model and view
     */
    private void initDefaultState() {
        updateFilterDimensionLabels(null);
        cbxCombineMode.setValue(SeriesCombineMode.CONCAT);
    }

    /**
     * Returns the view viewModel of the ChartHeader component
     *
     * @return the view viewModel
     */
    public ChartHeaderModel getModel() {
        return viewModel;
    }

    /**
     * Returns the query filter parameters defined by the user in the filter panel.
     * The parameters will returned as {@link QueryFilterParams} instance that contains all defined
     * filters that will send to as query to persistence layer. The filters didn't include
     * post processing settings like threshold or smoothing type.
     *
     * @return all filter packed into a QueryFilterParams instance
     */
    public QueryFilterParams getDefinedFilterParameters() {
        return viewModel.asQueryParams();
    }

    /**
     * Returns the (post) compute parameters defined by the user in the filter panel.
     * The parameters will returned as {@link QueryComputeParams} instance that contains all defined settings
     * and should use to control the post processing steps on fetched time series data.
     *
     * The settings doesn't include filter parameters send as query to the persistence layer.
     *
     * @return all compute parameters packed into a QueryComputeParams instance
     */
    public QueryComputeParams getDefinedComputedParameters() {
        return viewModel.asComputeParams();
    }

    /**
     * Returns a reference to the owner window the chart header panel
     * belongs to.
     *
     * @return the owner window
     */
    public Window getOwnerWindow() {
        return btRefreshGraph.getScene().getWindow();
    }

    /**
     * Event listener method that will switch the view flavor (classic filters vs cloud filter) if
     * it is notified by an ProjectViewFlavorChangedEvent event
     *
     * @param event ProjectViewFlavorChangedEvent instance with the new state
     */
    @SuppressWarnings("unused") // used by event bus via reflection
    @EkgEventSubscriber(eventClass = ProjectViewFlavorChangedEvent.class)
    public void changeProjectViewFlavor(ProjectViewFlavorChangedEvent event) {
        if (viewModel.getCurrentProject().getProjectFlavor() == ProjectFlavor.CLASSIC
                && event.getNewViewFlavor() == ProjectViewFlavor.LOGICAL_VIEW) {
            return;

        } else if (viewModel.getCurrentProject().getProjectFlavor() == ProjectFlavor.CLOUD_NATIVE
                && event.getNewViewFlavor() == ProjectViewFlavor.PHYSICAL_VIEW){
            return;
        }
        viewModel.cloudModeProperty().setValue(event.getNewViewFlavor().equals(ProjectViewFlavor.LOGICAL_VIEW));
    }

    /**
     * Sets the expertQuery context.
     *
     * @param queryContext a expertQuery context.
     */
    public void setQueryContext(QueryFilterParams queryContext, OpeningMode openingMode) {

        if (openingMode == OpeningMode.MERGE_VIEW &&
                    !viewModel.getCurrentProject().equals(queryContext.getProject())) {
        }

        viewModel.loadDataFromQueryContext(queryContext);
        updateFilterDimensionLabels(queryContext.getProject());
    }

    /**
     * Sets the expertQuery context, but this method does not(!) change the value of the expertMode and the
     * MultiMetricMode.
     */
    public void setQueryContextWithoutModes(QueryFilterParams queryContext) {

        updateFilterDimensionLabels(queryContext.getProject());
        boolean oldExpertMode = viewModel.isExpertMode();
        boolean oldMultiMetricMode = viewModel.isMultiMetricMode();

        viewModel.loadDataFromQueryContext(queryContext);

        viewModel.setExpertMode(oldExpertMode);
        viewModel.setMultiMetricMode(oldMultiMetricMode);
    }

    /**
     * Get the current types.
     *
     * @return the current types.
     */
    public EkgRepository getRepository() {
        return viewModel.getRepository();
    }

    /**
     * Set the EKG repository that will used by the controller to fetch the
     * facet data for the filter combo boxes.
     *
     * @param repository the repository the stores the project which is the context for all settings in the panel
     */
    public void setRepository(EkgRepository repository) {
        viewModel.setRepository(repository);
    }

    /**
     * Sets the current multi metric mode
     *
     * @param multiMetricMode the new value of the multi metric mode true = active, false = inactive
     */
    public void setMultiMetricMode(boolean multiMetricMode) {
        viewModel.setMultiMetricMode(multiMetricMode);
    }


    //------------------------------------------------------------------------------------------------------------------
    // internal initialization implementation
    //------------------------------------------------------------------------------------------------------------------

    private void initViewStyles() {
        Group iconImageUpdate = new SvgLoader().loadSvg(getClass().getResourceAsStream("update-icon.svg"));
        iconImageUpdate.setScaleX(0.05);
        iconImageUpdate.setScaleY(0.05);
        btRefreshGraph.setGraphic(new Group(iconImageUpdate));

        Group iconImageBookmark = new SvgLoader().loadSvg(getClass().getResourceAsStream("bookmark-icon.svg"));
        iconImageBookmark.setScaleX(1.2);
        iconImageBookmark.setScaleY(1.2);
        btBookmark.setGraphic(new Group(iconImageBookmark));

        cbxSmoothingType.setConverter(new StringConverter<>() {
            @Override
            public String toString(SeriesSmoothingType mode) {
                return mode.getName();
            }

            @Override
            public SeriesSmoothingType fromString(String alias) {
                return SeriesSmoothingType.NONE;
            }
        });

        cbxSmoothingGranularity.setConverter(new NamedEnumConverter<>());
        cbxSmoothingType.setConverter(new NamedEnumConverter<>());
        cbxCombineMode.setConverter(new NamedEnumConverter<>());
    }

    private void updateFilterDimensionLabels(Project project) {

        if (project == null) {
            project = new Project(null, null, null, null);
        } else {
            ProjectFlavor projectFlavor = project.getProjectFlavor();
            viewModel.cloudModeProperty().setValue(projectFlavor != ProjectFlavor.CLASSIC);
        }

        lbHostGroup.setText(getDimensionLabel(FilterDimension.HOST_GROUP, project.getDimensionAliasHostGroup()));
        lbHost.setText(getDimensionLabel(FilterDimension.HOST, project.getDimensionAliasHost()));
        lbNameSpace.setText(getDimensionLabel(FilterDimension.NAMESPACE, project.getDimensionAliasNamespace()));
        lbService.setText(getDimensionLabel(FilterDimension.SERVICE, project.getDimensionAliasService()));
        lbPod.setText(getDimensionLabel(FilterDimension.POD, project.getDimensionAliasPod()));
        lbContainer.setText(getDimensionLabel(FilterDimension.CONTAINER, project.getDimensionAliasContainer()));
        lbMeasurement.setText(getDimensionLabel(FilterDimension.MEASUREMENT, project.getDimensionAliasMeasurement()));
        lbProcess.setText(getDimensionLabel(FilterDimension.PROCESS, project.getDimensionAliasProcess()));
        lbMetricGroup.setText(getDimensionLabel(FilterDimension.METRIC_GROUP, project.getDimensionAliasMetricGroup()));
        lbMetric.setText(getDimensionLabel(FilterDimension.METRIC_NAME, project.getDimensionAliasMetricName()));

        disableFilterWithNoData();
    }

    private void disableFilterWithNoData(){
        for (Node child : seriesFilterGroup.getChildren()) {
            Label label = getLabelFromFilterPane(child);

            if (label != null && label.getText().equalsIgnoreCase("N/D:")) {
                child.visibleProperty().unbind();
                child.setVisible(false);
            }
        }
    }

    private Label getLabelFromFilterPane(Node filterPane) {
        if (!(filterPane instanceof Pane)) {
            return null;
        }
        // return first label in filterPane
        for (Node child : ((Pane) filterPane).getChildren()) {
            if (child instanceof Label) {
                return (Label) child;
            }
        }
        return null;
    }

    private String getDimensionLabel(FilterDimension dimension, String projectSpecificLabel) {
        if (StringUtils.isBlank(projectSpecificLabel)) {
            projectSpecificLabel = ChartHeaderModel.DEFAULT_FILTER_DIMENSION_LABELS.get(dimension);
        }

        if (!projectSpecificLabel.endsWith(":")) {
            projectSpecificLabel += ":";
        }

        return projectSpecificLabel;
    }

    private void configureExtendedFilterAccessibility() {
        cbEnableAllFilter.disableProperty().bind(viewModel.cloudModeProperty().not().or(viewModel.getExpertModeProperty()));
        lbEnableAllFilter.disableProperty().bind(cbEnableAllFilter.disableProperty());

        labelExpertQuery.visibleProperty().bind(viewModel.getExpertModeProperty());
        taExpertQuery.disableProperty().bind(viewModel.getExpertModeProperty().not());
        taExpertQuery.visibleProperty().bind(viewModel.getExpertModeProperty());

        labelExclude.visibleProperty().bind(viewModel.getExpertModeProperty().not());
        txtExclude.disableProperty().bind(viewModel.getExpertModeProperty());
        txtExclude.visibleProperty().bind(viewModel.getExpertModeProperty().not());

        lbMetric.visibleProperty().bind(viewModel.getExpertModeProperty().not());
        cbxMetric.disableProperty().bind(viewModel.getExpertModeProperty());
        cbxMetric.visibleProperty().bind(viewModel.getExpertModeProperty().not());

        startPicker.disableProperty().bind(viewModel.getExpertModeProperty());
        stopPicker.disableProperty().bind(viewModel.getExpertModeProperty());
    }

    private void configureClassicOrCloudFilterAccessibility() {

        BooleanBinding enableExtCloudFilters = viewModel.cloudModeProperty().not()
                .or(viewModel.cloudModeProperty().and(cbEnableAllFilter.selectedProperty()));

        measurementFilterPane.visibleProperty().bind(enableExtCloudFilters);
        measurementFilterPane.managedProperty().bind(measurementFilterPane.visibleProperty());

        serviceFilterPane.visibleProperty().bind(viewModel.cloudModeProperty().and(cbEnableAllFilter.selectedProperty()));
        serviceFilterPane.managedProperty().bind(serviceFilterPane.visibleProperty());


        for (Node child : seriesFilterGroup.getChildren()) {

            initDisableAndEditableBehavior(child);

            // don't consume space in the region if invisible
            child.managedProperty().bind(child.visibleProperty());

            if (child.getStyleClass().contains("classicFilter")) {
                child.visibleProperty().bind(viewModel.cloudModeProperty().not());

            } else if (child.getStyleClass().contains("cloudFilter") && child != serviceFilterPane) {
                child.visibleProperty().bind(viewModel.cloudModeProperty());
            }
        }
    }

    private void initDisableAndEditableBehavior(Node filterPane) {

        if (!(filterPane instanceof Pane)) {
            return;
        }

        for (Node child : ((Pane)filterPane).getChildren()) {
            child.disableProperty().bind(viewModel.getExpertModeProperty());

            if (child instanceof ComboBox) {
                ((ComboBox)child).setEditable(true);
            }
        }
    }

    /**
     * Bind the series and generic filter controls (ComboBoxes)
     * to the view model. The binding will be bidirectional.
     */
    private void bindFilterToModel() {

        // classic series filter
        Bindings.bindComboBox(cbxHostGroup, viewModel.getHostGroups(), viewModel.currentHostGroupProperty());
        Bindings.bindComboBox(cbxHost, viewModel.getHosts(), viewModel.currentHostProperty());

        // cloud native series filter
        Bindings.bindComboBox(cbxNamespace, viewModel.getNamespaces(), viewModel.currentNamespaceProperty());
        Bindings.bindComboBox(cbxService, viewModel.getServices(), viewModel.currentServiceProperty());
        Bindings.bindComboBox(cbxPod, viewModel.getPods(), viewModel.currentPodProperty());
        Bindings.bindComboBox(cbxContainer, viewModel.getContainers(), viewModel.currentContainerProperty());

        // overall series filter
        Bindings.bindComboBox(cbxMeasurement, viewModel.getMeasurements(), viewModel.currentMeasurementProperty());
        Bindings.bindComboBox(cbxProcess, viewModel.getProcess(), viewModel.currentProcessProperty());
        Bindings.bindComboBox(cbxMetricGroup, viewModel.getMetricMetricGroups(), viewModel.currentMetricGroupProperty());
        Bindings.bindComboBox(cbxMetric, viewModel.getMetrics(), viewModel.currentMetricProperty());

        // other filters
        txtExclude.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setExcludeMetric(new Metric(txtExclude.getText())));
        viewModel.rawQueryProperty().bindBidirectional(taExpertQuery.textProperty());
        Bindings.bindDatePicker(startPicker, viewModel.startDateProperty(), new InstantToUtcDateConverter());
        Bindings.bindDatePicker(stopPicker, viewModel.stopDateProperty(), new InstantToUtcDateConverter());

        // query/compute settings
        Bindings.bindComboBox(cbxSmoothingType, observableList(asList(SeriesSmoothingType.values())), viewModel.currentSmoothingTypeProperty());
        Bindings.bindComboBox(cbxSmoothingGranularity, observableList(asList(SeriesSmoothingGranularity.values())), viewModel.smoothingGranularityProperty());
        Bindings.bindComboBox(cbxThreshold, observableList(viewModel.getThresholds()), viewModel.thresholdProperty());
        Bindings.bindComboBox(cbxCombineMode, observableList(asList(SeriesCombineMode.values())), viewModel.seriesCombineModeProperty());

    }

    /**
     * Set a special kind of cell factory for the threshold combobox that will
     * use to format the value in a better readable way.
     */
    private void initComboBoxCellFactories() {

        Callback<ListView<Integer>, ListCell<Integer>> thresholdRenderer = new Callback<>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || item < 0) {
                            setText("Off");
                        } else if (item == 0) {
                            setText("Only changes");
                        } else {
                            setText(item / 1000 + "K points/line");
                        }
                    }
                };
            }
        };

        cbxThreshold.setButtonCell(thresholdRenderer.call(null));
        cbxThreshold.setCellFactory(thresholdRenderer);
    }

    /* -------------------------------------------- event implementation -------------------------------------------- */

    /**
     * Initializes the controller.
     */
    private void initHandler() {

        CreateGraphEventHandler createGraphHandler = new CreateGraphEventHandler();
        txtExclude.setOnAction(createGraphHandler);
        startPicker.setOnAction(createGraphHandler);
        stopPicker.setOnAction(createGraphHandler);

        cbxSmoothingGranularity.valueProperty().addListener((s, o, n) -> actions.changeSmoothingGranularity(n));
        cbxSmoothingType.valueProperty().addListener((s, o, n) -> actions.changeSmoothingType(n));
        cbxCombineMode.valueProperty().addListener((s, o, n) -> actions.changeSeriesCombineMode(n));
        cbxThreshold.valueProperty().addListener((s, o, n) -> actions.changeThreshold(n));

        // ====== init filter actions and default value for each filter box  ======

        // classic filter
        initCbxItemAction(cbxHostGroup,   viewModel::loadHostGroups,   new HostGroup(LOADING_TEXT),   HostGroup::new);
        initCbxItemAction(cbxHost,        viewModel::loadHosts,        new Host(LOADING_TEXT),        Host::new);
        // cloud filter
        initCbxItemAction(cbxNamespace,   viewModel::loadNamespaces,   new Namespace(LOADING_TEXT),   Namespace::new);
        initCbxItemAction(cbxService,     viewModel::loadServices,     new Service(LOADING_TEXT),     Service::new);
        initCbxItemAction(cbxPod,         viewModel::loadPods,         new Pod(LOADING_TEXT),         Pod::new);
        initCbxItemAction(cbxContainer,   viewModel::loadContainers,   new Container(LOADING_TEXT),   Container::new);
        // generic filter
        initCbxItemAction(cbxMeasurement, viewModel::loadMeasurements, new Measurement(LOADING_TEXT), Measurement::new);
        initCbxItemAction(cbxProcess,     viewModel::loadProcesses,    new Process(LOADING_TEXT),     Process::new);
        initCbxItemAction(cbxMetricGroup, viewModel::loadMetricGroups, new MetricGroup(LOADING_TEXT), MetricGroup::new);
        initCbxItemAction(cbxMetric,      viewModel::loadMetrics,      new Metric(LOADING_TEXT),      Metric::new);
    }


    private <T> void initCbxItemAction(final FilterableComboBox<T> comboBox, Runnable loader,
                                       T firstDummyItem, Supplier<T> dummyItemCreator) {

        // add default value to each combo-box (normally a wildcard)
        List<T> dummyItems = initDummyItems(firstDummyItem, dummyItemCreator);
        comboBox.getItems().setAll(dummyItems);
        comboBox.setVisibleRowCount(10);

        comboBox.focusedProperty().addListener(e -> {
            // if the box has focus and is marked as "not updated" or still contains the initial dummy item
            // then load the data from server
            if (comboBox.isFocused() && (!updatedComboboxes.contains(comboBox) ||
                    (!comboBox.getItems().isEmpty() && firstDummyItem.equals(comboBox.getItems().get(0))))) {
                loader.run();
                updatedComboboxes.add(comboBox);
            }
        });

        comboBox.setOnMouseClicked(e -> {
            // if the user clicks on the arrow down button of the combo-box update it's model (each time)
            if (e.getTarget() instanceof StackPane && "arrow-button".equals(((StackPane)e.getTarget()).getId())) {
                loader.run();
                updatedComboboxes.add(comboBox);
            }
        });

        // if the user change a combobox value and leave it the states of all other boxes will invalidated.
        // On this way it will ensured that every box get the current data that are valid for the all selected filters
        comboBox.setOnSelectionCompleted(e -> {
            updatedComboboxes.clear();
            actions.updateMetricsGraph();
        });
    }


    /**
     * Creates a list with dummy items which should be shown while the backend system is calculating the real items.
     *
     * @param first the first item.
     * @param other a generator for the other items.
     * @param <T>   the actual type of all items.
     * @return a list with the created items.
     */
    private <T> List<T> initDummyItems(T first, Supplier<T> other) {
        List<T> dummyItems = new ArrayList<>();
        dummyItems.add(first);
        for (int i = 0; i < NUMBER_DUMMY_LOADING_ITEMS; i++) {
            dummyItems.add(other.get());
        }
        return dummyItems;
    }

    public void setComputeParameters(QueryComputeParams queryComputeParams) {

        viewModel.suppressEventHandling(true);

        viewModel.thresholdProperty().setValue(queryComputeParams.getThreshold());
        viewModel.seriesCombineModeProperty().setValue(queryComputeParams.getSeriesCombineMode());
        viewModel.smoothingGranularityProperty().setValue(queryComputeParams.getSeriesSmoothingGranularity());
        viewModel.currentSmoothingTypeProperty().setValue(queryComputeParams.getSeriesSmoothingType());
        viewModel.seriesCombineModeProperty().setValue(queryComputeParams.getSeriesCombineMode());

        viewModel.suppressEventHandling(false);
    }

    public class CreateGraphEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            if (!viewModel.isSuppressEventHandling()) {
                actions.updateMetricsGraph();
            }
        }
    }

    /**
     * Callback for handling actions
     */
    @SuppressWarnings("all")
    public static class ActionCallbacks {
        /**
         * Handles the button clicked event on the addToGraphButton
         */
        public void pushSnapshot() {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Deletes all Metrics from the Graph.
         */
        public void clearCharts() {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Creates the Metrics for the Graph.
         */
        public void updateMetricsGraph() {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Creates a new bookmark. A Dialog is shown, in this dialog the user can choose the bookmark's name.
         */
        public void createBookmark() {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Changes the Sampling smoothingGranularity
         *
         * @param smoothingGranularity new Sampling smoothingGranularity
         */
        public void changeSmoothingGranularity(@SuppressWarnings("unused") SeriesSmoothingGranularity smoothingGranularity) {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Changes the smoothing type for the sampling
         *
         * @param aggregation new smoothing type for the sampling
         */
        public void changeSmoothingType(@SuppressWarnings("unused") SeriesSmoothingType aggregation) {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Sets the combine mode which controls if multiple time series should combine to a single one
         * and if which algorithm should be used.
         *
         * @param combineMode the combine mode that will be used (NONE for disable combining)
         */
        public void changeSeriesCombineMode(@SuppressWarnings("unused") SeriesCombineMode combineMode) {

        }

        /**
         * Changes the graph type
         *
         * @param graphType new graph type
         */
        public void changeGraphType(@SuppressWarnings("unused") String graphType) {
            // Default implementation does nothing; can be overridden
        }

        /**
         * Changes the threshold of the points of the chart
         *
         * @param threshold the threshold of the points of the chart
         */
        public void changeThreshold(@SuppressWarnings("unused") int threshold) {
            // Default implementation does nothing; can be overridden
        }
    }
}