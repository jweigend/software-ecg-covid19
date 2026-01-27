package de.qaware.ekg.awb.importer.owidcovidonline;

import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.importer.owidcovidonline.bl.OwidCovidOnlineImportStrategy;
import de.qaware.ekg.awb.importer.owidcovidonline.ui.ImportStatusController;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryContextEvent;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.project.api.ProjectDataAccessService;
import de.qaware.ekg.awb.project.api.events.ProjectSelectedEvent;
import de.qaware.ekg.awb.project.api.model.Project;
import de.qaware.ekg.awb.repository.bl.repositories.SolrEmbeddedRepository;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ImporterTreeIconProvider;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectTimeSeriesType;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingDefinition;
import de.qaware.ekg.awb.sdk.awbapi.repository.AliasMappingProvider;
import de.qaware.ekg.awb.sdk.awbapi.repository.FilterDimension;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.importer.ImportModule;
import de.qaware.ekg.awb.sdk.importer.api.ClassicViewFlavorSupport;
import de.qaware.ekg.awb.sdk.importer.api.LocalProjectSupport;
import de.qaware.ekg.awb.sdk.importer.tasks.LocalProjectImportStrategy;
import de.qaware.ekg.awb.sdk.importer.ui.dialogs.ImportDialog;
import de.qaware.ekg.awb.sdk.importer.ui.dialogs.fileimport.FileImportDialog;
import de.qaware.ekg.awb.sdk.importer.ui.skins.DefaultImportTreeIconProvider;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Import module that will automatically import OWID COVID data from online sources
 * as time series data to Software EKG.
 */
@SuppressWarnings("unused") // used by CDI scan and automatic instantiation
public class OwidCovidOnlineImportModule extends ImportModule
        implements AliasMappingProvider, LocalProjectSupport, ClassicViewFlavorSupport {

    /**
     * The generic FileImportDialog used to setup an import procedure
     */
    private FileImportDialog importDialog;

    private ApplicationManipulator applicationManipulator = new ApplicationManipulator();

    //------------------------------------------------------------------------------------------------------------------
    // implementation of ImportModule API (implement abstract methods)
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public ProjectTimeSeriesType getSupportedProjectType() {
        return ProjectTimeSeriesType.SPECIALIZED;
    }

    @Override
    public ImporterTreeIconProvider getTreeIconProvider() {
        return new DefaultImportTreeIconProvider();
    }

    @Override
    public ImportDialog getImportDialog() {
        if (importDialog == null) {
            importDialog = new FileImportDialog();
            importDialog.setWindowsTitle("OWID COVID-19 Online Importer");
            importDialog.setHeaderDescription("Automatic import of COVID-19 data from Our World In Data (OWID)");
        }

        return importDialog;
    }

    @Override
    public String getMenuText() {
        return "Import OWID COVID-19 data";
    }

    @Override
    public String getImporterID() {
        return "OWID";
    }


    //------------------------------------------------------------------------------------------------------------------
    // implementation of AliasMappingProvider interface
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public AliasMappingDefinition getAliasMappingDefinition() {
        return dimensions -> {
            Map<FilterDimension, String> aliasMap = new EnumMap<>(FilterDimension.class);

            aliasMap.put(FilterDimension.MEASUREMENT, "N/D");
            aliasMap.put(FilterDimension.HOST_GROUP, "Continent");
            aliasMap.put(FilterDimension.HOST, "Country");
            aliasMap.put(FilterDimension.PROCESS, "N/D");
            aliasMap.put(FilterDimension.METRIC_GROUP, "Main category");
            aliasMap.put(FilterDimension.METRIC_NAME, "Metric");

            return aliasMap.get(dimensions);
        };
    }

    //------------------------------------------------------------------------------------------------------------------
    // implementation of LocalProjectSupport interface
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public LocalProjectImportStrategy getLocalProjectImportStrategy() {
        return new OwidCovidOnlineImportStrategy(importDialog.getSelectedFilesPaths());
    }

    /**
     * This method is called on the startup of the EKG.
     * - It configures (manipulates) the EKG-UI, so that some elements are not visible or clickable.
     * - Starts the download and import of the OWID-COVID-Data
     * - Shows the Download and Import Dialog
     * - Trigger a query, to show a default graph.
     */
    @Override
    public void start() {
        applicationManipulator.manipulateProjectBar();
        applicationManipulator.removeMenuEntries();

        openImportStatusDialog();
        DownloadAndImportTask downloadAndImportTask = new DownloadAndImportTask(this);
        VersionCheckerTask versionCheckerTask = new VersionCheckerTask();

        downloadAndImportTask.setOnFailed(v -> {
            // log error and inform user
            Exception e = new Exception(v.getSource().getException());
            EkgLogger.get().error("Error during download or import", e);
            EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
            eventBus.publish(new AwbErrorEvent(this, "An error has occurred while downloading and importing OWID data. Please try again later!", e));
        });

        downloadAndImportTask.setOnSucceeded(v -> {
            applicationManipulator.disableRightClickInExplorer();

            // small hack, to unfold the explorer
            EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
            eventBus.publish(new ExplorerUpdateEvent(this, null));

            showDefaultQuery();
        });

        downloadAndImportTask.start();
        versionCheckerTask.start();
    }

    /**
     * Shows after importing of the covid data a default graph. We selected Germany with metric new_cases.
     */
    private void showDefaultQuery() {
        EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
        Platform.runLater(() -> {

            // fires the event, that the project was selected
            eventBus.publish(new ProjectSelectedEvent(this, null, getProject(),
                    ProjectViewFlavor.PHYSICAL_VIEW, EkgLookup.lookup(SolrEmbeddedRepository.class)));

            // small hack to show a first default query
            eventBus.publish(new QueryContextEvent(this, getFirstQuery(), EkgLookup.lookup(SolrEmbeddedRepository.class), OpeningMode.CLEAR_VIEW));
        });
    }

    /**
     * Build ups a default query to show "Germany" with the metric "New Cases". It is used to show the user a default graph.
     *
     * @return A query to show Germany with new_cases.
     */
    private QueryFilterParams getFirstQuery() {
        return new QueryFilterParams.Builder()
                // Process == Country, we have an alias on that
                .withHost("Germany")
                .withMetricGroup("CASES")
                .withMetric("new_cases")
                .withProject(getProject())
                .withMultiMetricMode(false)
                .build();
    }

    /**
     * Finds the project object in the solr repository for this plugin.
     *
     * @return The project object.
     */
    private Project getProject() {
        ProjectDataAccessService service = EkgLookup.lookup(ProjectDataAccessService.class);
        service.initializeService(EkgLookup.lookup(SolrEmbeddedRepository.class).getRepositoryClient());

        Optional<Project> project = service.listProjects().stream().filter(p -> p.getName().startsWith(getImporterID() + " - ")).findFirst();
        return project
                .orElseThrow(() -> new IllegalStateException("There should be one project!"));
    }

    /**
     * Opens the download and import status dialog.
     * It shows the user the progress of the download and import of the covid data.
     */
    @SneakyThrows
    private void openImportStatusDialog() {
        // window open
        FXMLLoader fxmlLoader = Lookup.lookup(FXMLLoader.class);
        fxmlLoader.setLocation(ImportStatusController.class.getResource("ImportStatus.fxml"));
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("Downloading and Importing COVID-19 Data");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(EkgLookup.lookup(ApplicationWindow.class).getStage());
        stage.setResizable(false);


        // give the controller the stage to close the window
        ImportStatusController controller = fxmlLoader.getController();
        controller.setStage(stage);
        // prevent the closing of the import window
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, controller::handleCloseEvent);

        stage.show();
    }
}