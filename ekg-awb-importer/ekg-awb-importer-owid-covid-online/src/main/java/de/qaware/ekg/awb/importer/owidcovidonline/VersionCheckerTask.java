package de.qaware.ekg.awb.importer.owidcovidonline;

import de.qaware.ekg.awb.importer.owidcovidonline.ui.VersionCheckerController;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import javafx.concurrent.Task;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Checks if new version of plugin is available.
 * First, a json file containing the latest version available is downloaded from a given URL.
 * Then, the local version is determined and compared to the latest version information.
 */
public class VersionCheckerTask extends Task<Void> {

    private static final String VERSION_FILE_DOWNLOAD_URL =
            "https://info.weigend.de/currentVersion.json";

    private static final Logger LOGGER = EkgLogger.get();

    /**
     * Starting task in a thread.
     */
    public void start() {
        new Thread(this).start();
    }

    @Override
    protected Void call() {
       /* String availableVersion = downloadVersionInfo();
        if (availableVersion == null) {
            return null;
        }
        String localVersion = getClass().getPackage().getImplementationVersion();
        if (localVersion == null) {
            LOGGER.warn("Local implementation version could not be determined");
            return null;
        }
        if (availableVersion.equals(localVersion)) {
            LOGGER.info("Software version {} is up-to-date", localVersion);
        } else {
            VersionCheckerController versionCheckerController = new VersionCheckerController();
            versionCheckerController.showAlert();
            LOGGER.info("A new software version ({}) is available", availableVersion);
        }*/
        return null;
    }

    // 200 and 300 are well-known HTTP response codes
    @SuppressWarnings("java:S109")
    private String downloadVersionInfo() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(VERSION_FILE_DOWNLOAD_URL).openConnection();
            con.connect();
            int statusCode = con.getResponseCode();
            if (200 <= statusCode && statusCode < 300) {
                String versionFileToString = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(versionFileToString);
                return (String) jsonObject.get("version");
            } else {
                LOGGER.error("Latest available software version could not be downloaded. HTTP Status Code: {}", statusCode);
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Latest available software version could not be determined!", e);
            return null;
        } catch (ParseException e) {
            LOGGER.error("Latest available software version could not be determined!" +
                    "Error while parsing version file", e);
            return null;
        }
    }
}
