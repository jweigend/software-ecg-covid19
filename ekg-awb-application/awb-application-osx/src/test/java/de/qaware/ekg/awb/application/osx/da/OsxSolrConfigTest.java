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
package de.qaware.ekg.awb.application.osx.da;

import de.qaware.ekg.awb.da.solr.embedded.DefaultSolrConfig;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for the {@link OsxSolrConfig}.
 */
public class OsxSolrConfigTest {
    public static final String JAVA_LIBRARY_PATH = "java.library.path";
    public static final String USER_HOME = "user.home";
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private String originLibraryPath;
    private String originUserHome;

    @Before
    public void setUp() throws Exception {
        originLibraryPath = System.getProperty(JAVA_LIBRARY_PATH);
        originUserHome = System.getProperty(USER_HOME);
        File file = new File(getClass().getClassLoader().getResource("solr/solr.xml").toURI()).getParentFile();
        System.setProperty(JAVA_LIBRARY_PATH, file.toString());
        System.setProperty(USER_HOME, file.getParent());
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty(JAVA_LIBRARY_PATH, originLibraryPath);
        System.setProperty(USER_HOME, originUserHome);
    }

    @Test
    public void testInit() throws Exception {
        OsxSolrConfig osxSolrConfig = new OsxSolrConfig();
        osxSolrConfig.init();
        String baseDir = System.getProperty(DefaultSolrConfig.SOLR_BASE_DIR_PROPERTY);
        File solrDir = new File(baseDir.split(PATH_SEPARATOR)[0]);
        assertThat(solrDir.exists(), is(true));

        osxSolrConfig.init();
        assertThat(System.getProperty(DefaultSolrConfig.SOLR_BASE_DIR_PROPERTY), containsString(PATH_SEPARATOR + baseDir));
        FileUtils.deleteDirectory(solrDir);
    }
}
