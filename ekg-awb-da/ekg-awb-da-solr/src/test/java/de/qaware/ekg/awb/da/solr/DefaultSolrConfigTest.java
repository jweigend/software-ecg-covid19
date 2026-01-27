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
package de.qaware.ekg.awb.da.solr;

import de.qaware.ekg.awb.da.solr.embedded.DefaultSolrConfig;
import de.qaware.ekg.awb.da.solr.embedded.SolrConfiguration;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for the {@link DefaultSolrConfig}.
 */
public class DefaultSolrConfigTest {
    private SolrConfiguration config = new DefaultSolrConfig();

    @Test
    public void testGetSolrConfig() throws Exception {
        Path actual = config.getSolrConfig().toPath();
        Path expected = Paths.get(config.getSolrHomeDir().toString(), "solr.xml");
        assertThat(actual, is(equalTo(expected)));
    }
}
