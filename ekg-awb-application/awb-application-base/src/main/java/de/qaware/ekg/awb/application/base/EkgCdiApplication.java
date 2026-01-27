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
package de.qaware.ekg.awb.application.base;

import de.qaware.ekg.awb.application.base.module.ModuleBinding;
import de.qaware.ekg.awb.common.ui.events.FinishEvent;
import de.qaware.ekg.awb.commons.about.VersionInfo;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.main.CDIMain;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Startup class for the full Software-EKG with pre loader and cdi as
 * lookup mechanism.
 */
public class EkgCdiApplication extends CDIMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(EkgCdiApplication.class);

    @Override
    public void init() {
        super.init();
        Locale.setDefault(Locale.ENGLISH);
        // Disabled stderr redirection to avoid StackOverflowError with SLF4J simple logger
        // System.setErr(new PrintStream(System.err) {
        //     public void print(String s) {
        //         if (StringUtils.isNotBlank(s)) {
        //             LOGGER.error("Captured from stderr: {}", s);
        //         }
        //     }
        // });
    }

    @Override
    public void start(Stage stage) throws PlatformException {
        System.out.println("====== EkgCdiApplication.start() CALLED ======");
        System.out.flush();
        LOGGER.info("====== EkgCdiApplication.start() method entered ======");
        try {
            VersionInfo versionInfo = Lookup.lookup(VersionInfo.class);
            LOGGER.info("Starting Software-EKG {} Revision {}", versionInfo.getVersionString(), versionInfo.getBuildRevision());

            // bind EKG CDI and EventBus support
            ModuleBinding.bind();

            EkgLookup.lookup(EkgEventBus.class).subscribe(FinishEvent.class, event -> {
                try {
                    stop();
                    return true;
                } catch (Exception e) {
                    LOGGER.error("Error occurred at top application platform");
                }

                return false;
            });

            EkgLookup.lookup(RootWindowBootstraper.class).start(stage, getParameters());
            super.start(stage);

            ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
            appWindow.setTitle("Software EKG " + versionInfo.getVersionString() + " rev:" + versionInfo.getBuildRevision());
        } catch (Exception e) {
            LOGGER.warn("Can not startup EGK-Application", e);
            throw new PlatformException("caught exception during initialization of CDIMain", e);
        }
    }

    /**
     * Starter for thumb IDEs.
     *
     * @param args args.
     */
    public static void main(String[] args) {
        Application.launch(args);


    }
}
