package de.qaware.ekg.awb.metricanalyzer.ui.chartng.export;

import de.qaware.ekg.awb.sdk.core.config.PropertyConfigStore;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * A controller that manages the view of the ScreenshotExportDialog component
 * and it's actions.
 */
public class ScreenshotExportController implements Initializable {

    /**
     * The path of the config key in the property file
     * used to store and fetch the last selected directory
     * of file browser dialog
     */
    private static final String DEFAULT_LOOKUP_DIR_KEY = "ekg-awb-screenshot-exporter.default_lookup_dir";

    private static final String TRANSPARENT_BG_VALUE = "Use transparent background";

    private static final String TRANSPARENT_BG_WHITE_AXIS_VALUE = "Transparent background + white axes";

    private static final String WHITE_BG_VALUE = "Use white background";

    //-----------------------------------------------------------------------------------------------------------------
    // FXML control that will injected
    //-----------------------------------------------------------------------------------------------------------------

    @FXML
    private DialogPane exportDialog;

    @FXML
    private ComboBox<ScreenshotDimension> cbExportDimension;

    @FXML
    private ComboBox<String> cbBackgroundFill;

    @FXML
    private TextField txtFilePath;

    @FXML
    private Button btnBrowse;

    //-----------------------------------------------------------------------------------------------------------------
    // controller defined members
    //-----------------------------------------------------------------------------------------------------------------

    /**
     * Config store to persist UI config states
     */
    private PropertyConfigStore configStore = new PropertyConfigStore();

    /**
     * The exporter that implement the logic to capture an image
     * of the chart in the specified ratio and write it to file system.
     */
    private ScreenshotExporter screenshotExporter = null;

    /**
     * A File instance that points to the location in the file system
     * the captured image should written to.
     */
    private File exportFile = null;

    /**
     * Initial directory used than open the file chooser.
     * The value will read and updated using the AWB property store.
     */
    protected String initialDirectory;

    //================================================================================================================
    // Controller API
    //================================================================================================================

    /**
     * Sets the exporter used to capture a screenshot of the
     * chart and write it to file system.
     * The exporter should know the chart because the controller doesn't.
     *
     * @param screenshotExporter the exporter with a referenced chart component.
     */
    public void setExporter(ScreenshotExporter screenshotExporter) {
        this.screenshotExporter = screenshotExporter;
    }

    //================================================================================================================
    // implementation of Initializable API and internal initializer
    //================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initialDirectory = configStore.getConfigValue(DEFAULT_LOOKUP_DIR_KEY);

        if (initialDirectory == null) {
            initialDirectory = System.getenv("user.home") != null ? System.getenv("user.home"): ".";
            configStore.setConfigValue(DEFAULT_LOOKUP_DIR_KEY, initialDirectory);
        }

        exportDialog.setHeaderText("Choose the correct settings to export the currently displayed charts into a PNG-file.");

        initComboBoxStates();
        initDialogButtons();

        btnBrowse.setOnAction(new BrowseFilesActionHandler());
    }

    private void initDialogButtons() {

        exportDialog.getButtonTypes().addAll(
                ButtonType.APPLY,
                ButtonType.CANCEL
        );

        Button saveBtn = (Button) exportDialog.lookupButton(ButtonType.APPLY);
        saveBtn.setText("Save");
        saveBtn.setDisable(true);

        txtFilePath.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isBlank(newValue)) {
                saveBtn.setDisable(true);
                return;
            }

            File exportFile = new File(newValue);
            if (exportFile.isDirectory() || !exportFile.getParentFile().isDirectory()) {
                saveBtn.setDisable(true);
                return;
            }

            this.exportFile = exportFile;
            saveBtn.setDisable(false);
        });
    }

    private void initComboBoxStates() {
        cbExportDimension.setItems(FXCollections.observableArrayList(
                Arrays.asList(ScreenshotDimension.values())));

        cbExportDimension.setValue(ScreenshotDimension.DIM_2_TO_1);

        cbExportDimension.setConverter(new StringConverter<>() {

            @Override
            public String toString(ScreenshotDimension dim) {
                return dim.getName();
            }

            @Override
            public ScreenshotDimension fromString(String string) {
                return null;
            }
        });

        cbBackgroundFill.setItems(FXCollections.observableArrayList(
                TRANSPARENT_BG_VALUE, TRANSPARENT_BG_WHITE_AXIS_VALUE, WHITE_BG_VALUE
        ));

        cbBackgroundFill.setValue(WHITE_BG_VALUE);
    }

    //================================================================================================================
    // event handling
    //================================================================================================================

    protected Void exportToPngSheet(ButtonType type) {

        if (type != ButtonType.APPLY || screenshotExporter == null) {
            return null;
        }

        screenshotExporter.export(
                cbExportDimension.getValue(),
                cbBackgroundFill.getValue().equals(WHITE_BG_VALUE),
                cbBackgroundFill.getValue().equals(TRANSPARENT_BG_WHITE_AXIS_VALUE),
                exportFile
        );

        return null;
    }

    private class BrowseFilesActionHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent actionEvent) {
            FileChooser saveFileChooser = new FileChooser();
            saveFileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Screenshot files", "*.png"),
                    new FileChooser.ExtensionFilter("All files", "*")
            );

            File initialDirectory = new File(ScreenshotExportController.this.initialDirectory);

            if (initialDirectory.isFile()) {
                initialDirectory = initialDirectory.getParentFile();
            }

            if (!initialDirectory.isDirectory() && initialDirectory.getParentFile().isDirectory()) {
                initialDirectory = initialDirectory.getParentFile();
            }

            if (!initialDirectory.exists() || !initialDirectory.canRead()) {
                ScreenshotExportController.this.initialDirectory = ".";
                initialDirectory = new File(ScreenshotExportController.this.initialDirectory);
            }

            saveFileChooser.setInitialDirectory(initialDirectory);

            //make file chooser modal by blocking parent window
            Scene scene = btnBrowse.getScene();
            Window window = scene.getWindow();

            File chosenFile = saveFileChooser.showSaveDialog(window);
            if (chosenFile != null) {

                if (chosenFile.isFile()) {
                    ScreenshotExportController.this.initialDirectory = chosenFile.getParentFile().getAbsolutePath();
                    configStore.setConfigValue(DEFAULT_LOOKUP_DIR_KEY, ScreenshotExportController.this.initialDirectory);
                } else {
                    ScreenshotExportController.this.initialDirectory = chosenFile.getAbsolutePath();
                    configStore.setConfigValue(DEFAULT_LOOKUP_DIR_KEY, ScreenshotExportController.this.initialDirectory);
                }

                String filePath = chosenFile.getAbsolutePath();

                if (!filePath.toLowerCase().endsWith(".png")) {
                    filePath += ".png";
                }

                txtFilePath.setText(filePath);
            }
        }
    }
}
