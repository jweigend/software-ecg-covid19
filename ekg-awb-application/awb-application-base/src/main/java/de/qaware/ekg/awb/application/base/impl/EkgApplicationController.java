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
package de.qaware.ekg.awb.application.base.impl;

import de.qaware.ekg.awb.application.base.about.AboutDialogController;
import de.qaware.ekg.awb.common.ui.events.FinishEvent;
import de.qaware.ekg.awb.common.ui.view.AppWindowProvider;
import de.qaware.ekg.awb.importer.owidcovidonline.CsvHelper;
import de.qaware.ekg.awb.importer.owidcovidonline.ManualCsvImportTask;
import de.qaware.ekg.awb.importer.owidcovidonline.ui.ImportStatusController;
import de.qaware.ekg.awb.project.ui.mgrdialog.CreateProjectDialog;
import de.qaware.ekg.awb.repository.ui.admin.EkgRepositoryAdmin;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Objects;

/**
 * This controller is very important!
 * It controls the action created by the user via the menu,
 * e.g. importing types, closing the project or showing the "About"-Dialog.
 * <p/>
 * Suppress warnings "unused" cause sonar can not detect usages directly from fxml.
 */
@Singleton
@Specializes
public class EkgApplicationController extends DefaultApplicationWindow {

    /**
     * URL to the Software EKG confluence page.
     */
    public static final String CONFLUENCE_URL = "https://www.weigend.de";

    public static final String CONFLUENCE_REFERENCE_MANUAL_URL = "https://www.weigend.de";

    private static final Logger LOGGER = EkgLogger.get();

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuItem confluenceLink;

    @FXML
    private MenuItem refManualLink;

    private String title;

    @Override
    public void init() {

        getStage().setMaximized(true);
        borderPane.setCenter(getWindowManager().getRootPane());

        useSystemMenuBarIfPossible();

        registerErrorHandler();
    }

    /**
     * Getter for property confluenceLink.
     *
     * @return Value for property confluenceLink.
     */
    public MenuItem getConfluenceLink() {
        return confluenceLink;
    }

    /**
     * Used for restoring the default Application layout.
     */
    @FXML
    public void restoreDefault() {
        //We don't have to call interface methods for execution!!!
        Lookup.lookup(WindowManager.class).restoreDefaultLayout();
    }

    /**
     * Setter for property centerArea.
     *
     * @param node Value to set for property centerArea.
     */
    @FXML
    @SuppressWarnings("unused")
    private void setCenterArea(Node node) {
    }

    /**
     * Shows the about dialog
     */
    @FXML
    private void handleAboutMenuItemAction() {
        try {
            FXMLLoader fxmlLoader = Lookup.lookup(FXMLLoader.class);
            fxmlLoader.setLocation(AboutDialogController.class.getResource("AboutDialog.fxml"));
            handleActionHelper(fxmlLoader.load(), "Software EKG 6.2.3 COVID-19 Edition");
        } catch (IOException e) {
            LOGGER.warn("Exception raised and with this I could not handle the about-menu button action", e);
        }
    }

    /**
     * Opens a file chooser to select a CSV file and imports the data.
     */
    @FXML
    private void handleImportCsvFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv", "*.csv.gz"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            // Validate the CSV file
            if (!CsvHelper.validateCsvHeaders(selectedFile.getAbsolutePath())) {
                showError("Invalid CSV File",
                        "The selected file does not appear to be a valid OWID COVID-19 data file.",
                        "Please select a CSV file with the correct format (containing columns like 'iso_code', 'location', 'date', etc.).");
                return;
            }

            // Open the import status dialog and start the import
            try {
                openImportStatusDialogForManualImport(selectedFile);
            } catch (IOException e) {
                LOGGER.error("Error opening import status dialog", e);
                showError("Import Error", "Could not start the import process.", e.getMessage());
            }
        }
    }

    /**
     * Opens the import status dialog for a manual CSV import.
     *
     * @param csvFile The CSV file to import
     */
    private void openImportStatusDialogForManualImport(File csvFile) throws IOException {
        FXMLLoader fxmlLoader = Lookup.lookup(FXMLLoader.class);
        fxmlLoader.setLocation(ImportStatusController.class.getResource("ImportStatus.fxml"));
        Parent parent = fxmlLoader.load();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("Importing CSV Data: " + csvFile.getName());
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.setResizable(false);

        // Give the controller the stage to close the window
        ImportStatusController controller = fxmlLoader.getController();
        controller.setStage(stage);
        // Prevent the closing of the import window
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, controller::handleCloseEvent);

        stage.show();

        // Start the import task
        ManualCsvImportTask importTask = new ManualCsvImportTask(csvFile);
        importTask.start();
    }

    /**
     * Shows an error dialog.
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(getStage());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/error-icon.png").toString()));
        alert.showAndWait();
    }

    /**
     * Opens the web browser and links to confluence.
     */
    @FXML
    private void handleConfluenceLinkAction() {
        URI u = URI.create(CONFLUENCE_URL);
        try {
            java.awt.Desktop.getDesktop().browse(u);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open browser to " + CONFLUENCE_URL, e);
        }
    }

    @FXML
    private void handleRefManualLinkAction() {
        URI u = URI.create(CONFLUENCE_REFERENCE_MANUAL_URL);
        try {
            java.awt.Desktop.getDesktop().browse(u);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open browser to " + CONFLUENCE_URL, e);
        }
    }

    /**
     * Exits the SoftwareEKG.
     */
    @FXML
    public void handleExitMenuItemAction() {
        //close program ordinary
        EkgLookup.lookup(EkgEventBus.class).publish(new FinishEvent(0, this));
    }

    /**
     * Opens a dialog to create a new project in a remote EKG repository
     * which will/can used by EKG collector than writing time series data directly to database instead
     * using CSV files.
     */
    @FXML
    @SuppressWarnings("unused")
    private void handleOpenAddProjectDialog() {
        CreateProjectDialog createProjectDialog = new CreateProjectDialog();
        createProjectDialog.initOwner(borderPane.getScene().getWindow());
        createProjectDialog.showAndWait();
    }

    /**
     * Used for handling SolrServer openings.
     */
    @FXML
    @SuppressWarnings("unused")
    private void handleOpenEkgRepositoryAdminDialog() {
        EkgRepositoryAdmin ekgRepositoryAdmin = new EkgRepositoryAdmin();
        ekgRepositoryAdmin.initOwner(borderPane.getScene().getWindow());
        ekgRepositoryAdmin.showAndWait();
    }

    @Override
    public void restoreTitle() {
        getStage().setTitle(title);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        if (getStage() != null) {
            getStage().setTitle(title);
        }
        this.title = title;
    }

    /**
     * This method does all necessary things to show a dialog.
     *
     * @param root  the root node of the scene graph
     * @param title the title for the dialog
     */
    private void handleActionHelper(Parent root, String title) {
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/ekg-app-icon2.png")));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        getStage().setTitle(getTitle());
        getStage().getIcons().add(new Image(getClass().getResourceAsStream("/ekg-app-icon2.png")));
    }

    @Produces
    @Default
    @ApplicationScoped
    public AppWindowProvider getDefaultWindow() {
        return new AppWindowProvider(borderPane.getScene().getWindow());
    }

    private void registerErrorHandler() {
        EkgEventBus bus = EkgLookup.lookup(EkgEventBus.class);
        bus.subscribe(AwbErrorEvent.class, event -> {
            Platform.runLater(() -> handleError((AwbErrorEvent) event));
            return true;
        });
    }

    private void handleError(AwbErrorEvent event) {

        Object source = event.getSource();
        Exception cause = event.getCause();


        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");

        if (event.hasCustomErrorMessage()) {
            alert.setHeaderText(event.getCustomErrorMessage());
        } else {
            alert.setHeaderText("An error occurred in the following class: " + source.getClass().getSimpleName());
        }

        if (cause != null) {
            alert.setContentText(cause.getMessage());
        }

        alert.getDialogPane().setMinWidth(800);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/error-icon.png").toString()));

        // Create expandable Exception.
        String exceptionText;
        if (cause != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);
            exceptionText = sw.toString();
        } else {
            exceptionText = "- no stacktrace -";
        }

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    @Override
    protected void platformShutdownRequestHandler(WindowEvent event) {
        Dialog<Boolean> d = new Dialog<>();
        d.initOwner(getStage());
        d.setResultConverter(b -> Objects.equals(b, ButtonType.YES));
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource("ShutdownDialog.fxml"));
        try {
            d.setDialogPane(loader.load());
            d.showAndWait().ifPresent(shouldClose -> {
                if (!shouldClose) {
                    event.consume();
                }
                else {
                    Platform.exit();
                }
            });
        }
        catch (IOException e) {
            LOGGER.error("Unable to load shutdown dialog.", e);
        }
    }

}
