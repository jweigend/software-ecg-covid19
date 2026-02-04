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

import org.apache.solr.core.CoreContainer;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Paths;

/**
 * Produces the core container for solr.
 */
@Singleton
public class CoreContainerProducer {

    @Inject
    private SolrConfiguration config;


    public CoreContainerProducer() {
    }

    /**
     * Produces the core container and inject the correct configuration.
     *
     * @return The fully instantiated core container.
     */
    @Produces
    @Singleton
    public CoreContainer createCoreContainer() {
        return CoreContainer.createAndLoad(
                Paths.get(config.getSolrHomeDir().toURI()),
                Paths.get(config.getSolrConfig().toURI())
        );
    }

    /**
     * Shutdown the core container.
     *
     * @param container The container to shutdown.
     */
    public void close(@Disposes CoreContainer container) {
        container.shutdown();
    }
}
