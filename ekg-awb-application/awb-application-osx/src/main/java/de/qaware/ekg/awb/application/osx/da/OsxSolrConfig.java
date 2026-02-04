//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.osx.da;

import de.qaware.ekg.awb.da.solr.embedded.DefaultSolrConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Specializes;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Special Solr Config for default Packaging under OSX.
 */
@Specializes
public class OsxSolrConfig extends DefaultSolrConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsxSolrConfig.class);

    /**
     * Initialize the OSX solr config.
     * <p>
     * Copy a fresh solr config into the users "Application Support" directory if needed.
     */
    @PostConstruct
    public void init() {
        String baseDirs = System.getProperty(SOLR_BASE_DIR_PROPERTY);
        Path solrHome = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "QAware",
                "Software-EKG", "embeddedSolr");

        baseDirs = StringUtils.isBlank(baseDirs) ? solrHome.toString() : solrHome + PATH_SEPARATOR + baseDirs;
        System.setProperty(SOLR_BASE_DIR_PROPERTY, baseDirs);

        setupCore(solrHome);
    }

    private void setupCore(Path solrHome) {
        Path solrXml = Paths.get(solrHome.toString(), "solr/solr.xml");
        if (solrXml.toFile().exists()) {
            return;
        }
        try {
            Path solrBaseDir = Paths.get(System.getProperty("java.library.path"), "..", "solr").toRealPath();

            LOGGER.info("Copy empty solr core to {}", solrHome);
            FileUtils.copyDirectory(solrBaseDir.toFile(), solrHome.toFile());
        } catch (IOException e) {
            LOGGER.error("Can not copy solr base dir. Try to find Solr Home through fallbacks", e);
        }
    }
}
