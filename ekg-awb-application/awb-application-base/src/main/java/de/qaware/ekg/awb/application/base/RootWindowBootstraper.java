package de.qaware.ekg.awb.application.base;

import de.qaware.ekg.awb.application.base.impl.EkgApplicationController;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.InputStream;

/**
 * Bootstrap class that will load the EKG JavaFX main view with CDI support
 *
 * The principe is documented in the the tutorial: http://www.tutego.de/blog/javainsel/2013/10/javafx-cdi-weld/
 */
public class RootWindowBootstraper {

    /**
     * this is a specialised version of FXMLLoader that provides CDI support
     * see {@link de.qaware.sdfx.extensions.cdi.contexts.FXMLLoaderProducer}
     */
    @Inject
    private FXMLLoader fxmlLoader;

    public void start(Stage stage, Application.Parameters parameters) throws PlatformException {
        try (InputStream fxml = EkgApplicationController.class
                .getResourceAsStream( "/de/qaware/ekg/awb/application/base/impl/Window.fxml" ) ) {

            Parent root = fxmlLoader.load(fxml);
            stage.setScene( new Scene( root ) );
            stage.show();
        } catch (Exception ex) {
            throw new PlatformException(ex);
        }
    }

}
