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
package de.qaware.ekg.awb.da.solr.embedded;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Default implementation for the configuration.
 * <p/>
 * It tries several locations to find the solr core. <ol> <li>Paths given in <code>ekg.solr.baseDir</code> system
 * property. This property allows multiple paths separated with : or ; (depends on os)</li>
 * <li><code>target/solr</code></li> <li><code>solr/</code></li> </ol>
 */
@Singleton
public class DefaultSolrConfig implements SolrConfiguration {

    public static final String SOLR_BASE_DIR_PROPERTY = "ekg.solr.baseDir";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolrConfig.class);

    private static final List<String> DEFAULT_SEARCH_PATHS = asList(
            "build/solr/", "solr/", "../solr/", "target/solr/", "target/test-classes/solr/"
    );

    protected static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private File solrConfig;

    @Override
    public File getSolrConfig() {
        if (solrConfig == null) {
            findSolrConfig();
        }
        return solrConfig;
    }

    @Override
    public File getSolrHomeDir() {
        if (solrConfig == null) {
            findSolrConfig();
        }
        return solrConfig.getParentFile();
    }

    /**
     * Find the solr.xml path.
     */
    private void findSolrConfig() {
        String externalConfig = System.getProperty(SOLR_BASE_DIR_PROPERTY);
        List<String> searchPaths = new ArrayList<>();
        if (StringUtils.isNotBlank(externalConfig)) {
            searchPaths.addAll(asList(externalConfig.split(PATH_SEPARATOR)));
        }
        searchPaths.addAll(DEFAULT_SEARCH_PATHS);
        for (String path : searchPaths) {
            File solrXml = Paths.get(path, "solr.xml").toFile();
            LOGGER.debug("Try to find solr.xml in {}", solrXml.getAbsolutePath());
            if (solrXml.exists()) {
                solrConfig = solrXml.getAbsoluteFile();
                return;
            }
        }
        throw new IllegalStateException("Can not find solr.xml");
    }
}
