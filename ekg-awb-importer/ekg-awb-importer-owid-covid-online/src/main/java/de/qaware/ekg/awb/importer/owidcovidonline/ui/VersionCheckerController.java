package de.qaware.ekg.awb.importer.owidcovidonline.ui;

import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Controller for VersionChecker
 */
public class VersionCheckerController {

    private static final Logger LOGGER = EkgLogger.get();
    private static final String DOWNLOAD_URL = "https://info.weigend.de/software-ekg-covid-edition";

    /**
     * Show new version is available window.
     */
    public void showAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(EkgLookup.lookup(ApplicationWindow.class).getStage());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("New version available");
            alert.setHeaderText("New version available");

            Hyperlink hyperlink = new Hyperlink("here.");
            hyperlink.setOnAction(evt -> {
                alert.close();
                try {
                    Desktop.getDesktop().browse(new URI(DOWNLOAD_URL));
                } catch (IOException | URISyntaxException e) {
                    LOGGER.error("Error while accessing download link", e);
                }
            });

            alert.getDialogPane().contentProperty().set(new TextFlow(
                    new Text("There is a new version of Software EKG - COVID-19 Edition. \n" +
                            "Please download latest version"),
                    hyperlink));
            alert.show();
        });
    }
}
