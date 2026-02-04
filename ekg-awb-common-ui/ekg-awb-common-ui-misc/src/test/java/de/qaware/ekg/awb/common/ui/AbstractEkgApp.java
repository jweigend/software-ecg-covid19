//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui;

import de.qaware.ekg.awb.common.ui.events.FinishEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.EkgEventSubscriberAnnotationProcessor;
import de.qaware.ekg.awb.sdk.core.lookup.EkgCDILookupStrategy;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Abstract Class to encapsulate all FX-Application test usage.
 */
public abstract class AbstractEkgApp extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEkgApp.class);
    /* The URL to the fxml which will be shown */
    private URL fxmlResource;

    /* The title which will be used */
    private String title;

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void setFxmlResource(URL fxmlResource) {
        this.fxmlResource = fxmlResource;
    }

    @Override
    public void init() {
        EkgCDILookupStrategy.initLookup();

        // Registers the ApplicationFinisher on the bus
        EkgLookup.lookup(EkgEventSubscriberAnnotationProcessor.class).process(new ApplicationFinisher());
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(fxmlResource);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Can not start application", e);
        }
    }

    @Override
    public void stop() {
        // Tells the ApplicationFinisher to Exit
        EkgLookup.lookup(EkgEventBus.class).publish(new FinishEvent(0, this));
    }
}