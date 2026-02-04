//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.base.about;

import de.qaware.ekg.awb.commons.about.VersionInfo;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ResourceBundle;

public class AboutDialogController implements Initializable {

    /**
     * The class logger
     */
    private static final Logger LOGGER = EkgLogger.get();

    @FXML
    private Label lblBuildDate;

    @FXML
    private Label lblRevision;

    @FXML
    private Label lblCopyrightYear;

    @FXML
    private Label lblProductVersion;

    @FXML
    private Label lblLicense;

    @FXML
    private Label lblJavaVersion;

    @FXML
    private Label lblRuntimeVersion;

    @FXML
    private Label lblSystemVersion;

    @FXML
    private Label lbFounderCredit;

    @FXML
    private Hyperlink linkHomepage;

    @FXML
    private ImageView ivAboutBanner;

    @FXML
    private ImageView ivFounderBanner;

    @FXML
    private Pane ivFounderBannerWrapper;

    @Inject
    private VersionInfo versionInfo;

    /**
     * Initialize the controller
     *
     * @param url            - the url
     * @param resourceBundle - the resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initHandlers();

        setSystemInformation();

        initBehaviour();
    }

    private void initBehaviour() {
        Image image = new Image(getClass().getResource("giphy.gif").toExternalForm());

        Rectangle rect = new Rectangle(0, 0, 800, 440);
        ivFounderBanner.setImage(image);
        ivFounderBanner.setFitWidth(800);
        ivFounderBanner.setClip(rect);

        ivFounderBannerWrapper.setVisible(false);
        ivFounderBannerWrapper.setMaxHeight(400);

    }


    /**
     * Sets the system information labels
     */
    private void setSystemInformation() {

        lblProductVersion.setText("Software EKG Version 6.2.3");
        lblRuntimeVersion.setText(ManagementFactory.getRuntimeMXBean().getVmName());
        lblJavaVersion.setText(System.getProperty("java.version"));
        lblLicense.setText("GPLv3");
        lblSystemVersion.setText(ManagementFactory.getOperatingSystemMXBean().getName());
        if (versionInfo != null) {
            lblCopyrightYear.setText(String.valueOf(LocalDateTime.ofInstant(versionInfo.getBuildTime(), ZoneId.systemDefault()).getYear()));
            lblBuildDate.setText(versionInfo.getBuildTime().toString());
            lblRevision.setText(versionInfo.getBuildRevision());
        }

        linkHomepage.setOnAction(event -> {
            openUrlInBrowser("http://covid19.weigend.de/");
        });
    }

    /**
     * Opens the given URL in the system's default browser.
     * Uses xdg-open on Linux, open on macOS, and rundll32 on Windows.
     * This approach avoids AWT/JavaFX threading conflicts that can cause freezes.
     *
     * @param url The URL to open
     */
    private void openUrlInBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        String[] command;
        
        if (os.contains("linux")) {
            command = new String[]{"xdg-open", url};
        } else if (os.contains("mac")) {
            command = new String[]{"open", url};
        } else if (os.contains("win")) {
            command = new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
        } else {
            LOGGER.warn("Unknown operating system: {}. Cannot open URL in browser.", os);
            return;
        }
        
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            LOGGER.warn("Could not open the URL '{}' in the default system browser.", url, e);
        }
    }

    /**
     * Inits the button handlers
     */
    private void initHandlers() {
        lbFounderCredit.setOnMouseClicked(event -> {
            founderClickHandler();
            lbFounderCredit.setText(ivFounderBannerWrapper.isVisible() ? "Got it!" : "Who invented it?!");
        });
    }

    private void founderClickHandler() {
        ivFounderBannerWrapper.setVisible(!ivFounderBannerWrapper.isVisible());
    }

    /**
     * Closes the about dialog
     *
     * @param event - the action event
     */
    private void closeWindow(ActionEvent event) {
        Node button = (Node) event.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
