//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui;

import de.qaware.ekg.awb.sdk.core.lookup.EkgCDILookupStrategy;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This is the viewer fxml test application.
 * <p>
 * It allows you to start a single fxml file inclusive the controller.
 * You can start every fxml file from classpath. The first command line
 * argument will be interpreted as the fxml file to start.
 * <p>
 * The started application has the same classpath elements as the full
 * application start and also uses the CDI lookup mechanism.
 */
public class Viewer extends Application {

    /**
     * Main entry point for starting the viewer fxml application.
     * <p>
     * This application require exact one argument. This argument must be the
     * path to a fxml file that should be initially loaded and set as scene root.
     *
     * @param args The first is required and used as the fxml to start.
     */
    public static void main(String[] args) {
        EkgCDILookupStrategy.initLookup();
        launch(Viewer.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        String path = getParameters().getRaw().get(0);
        FXMLLoader loader = EkgLookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource(path));
        Parent node = loader.load();
        primaryStage.setScene(new Scene(node));
        primaryStage.show();
    }
}

