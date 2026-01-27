//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.base;

import de.qaware.ekg.awb.commons.about.VersionInfo;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Get the Version Info for the Software-EKG
 */
@Singleton
public class EkgVersionInfo implements VersionInfo {
    private static final Logger LOGGER = EkgLogger.get();

    private String buildRevision;
    private Instant buildTime;
    private String versionString;

    /**
     * Read and cache the version properties.
     */
    public EkgVersionInfo() {
        Properties buildNumber = getBuildNumberProperties();
        versionString = buildNumber.getProperty("version");
        if (versionString == null) {
            versionString = "6.2.3 COVID-19 Edition";
        }

        buildRevision = buildNumber.getProperty("revision");
        if (buildRevision == null) {
            buildRevision = "6.2.3";
        }

        String timestamp = buildNumber.getProperty("timestamp");
        if (timestamp == null || timestamp.isEmpty() || timestamp.startsWith("$")) {

            Date date = getClassBuildTime();

            if (date != null) {
                buildTime = Instant.ofEpochMilli(date.getTime());
            } else {
                buildTime = Instant.ofEpochMilli(new Date().getTime());
            }
        } else {
            buildTime = Instant.parse(timestamp);
        }
    }

    @Override
    public String getBuildRevision() {
        return buildRevision;
    }

    @Override
    public Instant getBuildTime() {
        return buildTime;
    }

    @Override
    public String getVersionString() {
        return versionString;
    }

    /**
     * Get the build number properties.
     *
     * @return the build number properties.
     */
    private static Properties getBuildNumberProperties() {
        Properties buildNumber = new Properties();
        try {
            try (InputStream propertiesFile = EkgVersionInfo.class.getResourceAsStream("/build.properties")) {
                if (propertiesFile != null) {
                    buildNumber.load(propertiesFile);
                } else {
                    LOGGER.info("No Build Number found");
                }
            }
        } catch (IOException e) {
            LOGGER.info("No Build Number found");
        }
        return buildNumber;
    }


    private static Date getClassBuildTime() {
        Date d = null;
        Class<?> currentClass = new Object() {
        }.getClass().getEnclosingClass();
        URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
        if (resource != null) {
            switch (resource.getProtocol()) {
                case "file":
                    try {
                        d = new Date(new File(resource.toURI()).lastModified());
                    } catch (URISyntaxException ignored) {
                    }
                    break;
                case "jar": {
                    String path = resource.getPath();
                    d = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
                    break;
                }
                case "zip": {
                    String path = resource.getPath();
                    File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
                    try (JarFile jf = new JarFile(jarFileOnDisk)) {
                        ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2)); //Skip the ! and the /
                        long zeTimeLong = ze.getTime();
                        d = new Date(zeTimeLong);
                    } catch (IOException | RuntimeException ignored) {
                    }
                    break;
                }
            }
        }
        return d;
    }
}
