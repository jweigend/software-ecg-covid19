package de.qaware.ekg.awb.importer.owidcovidonline.ui;

import de.qaware.ekg.awb.importer.owidcovidonline.events.*;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBusListener;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.EventObject;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller of the Download and Import Status.
 */
public class ImportStatusController implements Initializable, EkgEventBusListener<EventObject> {

    /**
     * After a successful import, the window close automatically after this amount of seconds.
     */
    private static final int SECONDS_UNTIL_CLOSE = 1;
    /**
     * Alert window width in pixels.
     */
    private static final double ALERT_WINDOW_WIDTH = 500d;
    /**
     * Wait time for next task in millis.
     */
    private static final int WAIT_PERIOD_FOR_NEXT_TASK = 1000;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lbProgressText;

    @FXML
    private TextArea txtAreaLogMessages;

    @FXML
    private Button btnClose;

    private Stage stage;

    private boolean errorOccurred = false;

    private boolean finished = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);
        eventBus.subscribe(DownloadErrorEvent.class, this);
        eventBus.subscribe(ProgressEvent.class, this);
        eventBus.subscribe(DownloadAndImportFinishedEvent.class, this);
        eventBus.subscribe(DownloadProgressEvent.class, this);
        eventBus.subscribe(ImportProgressEvent.class, this);
        eventBus.subscribe(ImportErrorEvent.class, this);
    }


    @Override
    public boolean eventPublished(EventObject event) {
        if (event instanceof DownloadProgressEvent) {
            DownloadProgressEvent pEvent = (DownloadProgressEvent) event;
            setText(pEvent.getMessage(), pEvent.getShortMessage());
        } else if (event instanceof ImportProgressEvent) {
            ImportProgressEvent pEvent = (ImportProgressEvent) event;
            setText(pEvent.getMessage(), pEvent.getShortMessage());
        } else if (event instanceof DownloadErrorEvent) {
            errorOccurred = true;
            setText(((DownloadErrorEvent) event).getMessage());

            Platform.runLater(() -> {
                showAlert("Download Error", ((DownloadErrorEvent) event).getMessage());
            });
        } else if (event instanceof ImportErrorEvent) {
            errorOccurred = true;
            setText(((ImportErrorEvent) event).getMessage());

            Platform.runLater(() -> {
                showAlert("Download Error", ((ImportErrorEvent) event).getMessage());
            });
        } else if (event instanceof DownloadAndImportFinishedEvent) {
            Platform.runLater(() -> {
                progressBar.setProgress(1.0);
                if (!errorOccurred) {
                    lbProgressText.setText("Finished");
                } else {
                    lbProgressText.setText("Finished with warnings");
                }
                finished = true;
            });

            if (!errorOccurred) {
                closeCounter(SECONDS_UNTIL_CLOSE);
            } else {
                btnClose.setDisable(false);
            }

            btnClose.setOnAction(e -> stage.close());
        }
        return true;
    }

    /**
     * Callback to handle close event.
     *
     * @param event event which triggered the closing
     */
    public void handleCloseEvent(WindowEvent event) {
        if (!finished) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.setTitle("Should progress window be closed?");
            alert.setContentText("The download and import is in progress. You could close the progress window, but in the background will be further executed the download and import of the data.");
            alert.setHeaderText("Should progress window be closed?");
            alert.initOwner(stage.getOwner());
            alert.setWidth(ALERT_WINDOW_WIDTH);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            // set focus to NO button
            Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
            yesButton.setDefaultButton(false);
            Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
            noButton.setDefaultButton(true);

            ButtonType result = alert.showAndWait().orElseThrow();
            if (result == ButtonType.YES) {
                stage.close();
            }
            event.consume();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.initOwner(stage.getOwner());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }

    private void setText(String message) {
        setText(message, message);
    }

    private void setText(String message, String shortMessage) {
        // should be set in the FX-Thread.
        Platform.runLater(() -> {
            if (!finished) {
                lbProgressText.setText(shortMessage);
                txtAreaLogMessages.appendText(message + "\n");
            }
        });
    }

    private void closeCounter(int startCounter) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            private int counter = startCounter + 1;

            @Override
            public void run() {
                counter--;
                Platform.runLater(() -> {
                    btnClose.setDisable(false);
                    btnClose.setText(String.format("Closing in %s", counter));
                });
                if (counter == 0) {
                    Platform.runLater(() -> stage.close());
                    this.cancel();
                    timer.cancel();
                }
            }
        }, 0, WAIT_PERIOD_FOR_NEXT_TASK);
    }

}
