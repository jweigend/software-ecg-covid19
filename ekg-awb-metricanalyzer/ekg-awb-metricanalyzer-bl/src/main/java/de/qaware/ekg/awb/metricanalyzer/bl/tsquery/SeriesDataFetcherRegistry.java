package de.qaware.ekg.awb.metricanalyzer.bl.tsquery;

import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.importer.ImportModule;
import de.qaware.ekg.awb.sdk.importer.api.RemoteSeriesDataFetcher;
import de.qaware.ekg.awb.sdk.importer.api.SplitSourceProjectSupport;
import org.slf4j.Logger;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple map based registry used to define a single point of contact to
 * register and retrieve RemoteSeriesDataFetcher instances provides by different importers.
 * The registry will use application wide as singleton and should access via CDI lookup.
 */
@Default
@Singleton
public class SeriesDataFetcherRegistry {

    private static Logger LOG = EkgLogger.get();

    private Map<String, RemoteSeriesDataFetcher> registeredDataFetcher = new HashMap<>();

    private boolean initializationComplete = false;

    public void init() {

        List<ImportModule> modules = EkgLookup.lookupAll(ImportModule.class);

        if (modules == null) {
            LOG.error("Unable to retrieve import modules");
            return;
        }

        for (ImportModule module : modules) {
            if (module instanceof SplitSourceProjectSupport) {
                registerSeriesDataFetcher(module.getImporterID(),  ((SplitSourceProjectSupport) module).getSeriesDataFetcher());
            }
        }

        initializationComplete = true;
    }

    /**
     * Register a new instance of {@link RemoteSeriesDataFetcher} to the global registry.
     * If already a fetcher exists for the specified importerId, the existing one will overwritten.
     *
     * @param importerId the id of the importer
     * @param fetcher an instance of RemoteSeriesDataFetcher
     */
    public void registerSeriesDataFetcher(String importerId, RemoteSeriesDataFetcher fetcher) {
        registeredDataFetcher.put(importerId, fetcher);
    }

    /**
     * Return a {@link RemoteSeriesDataFetcher} that was registered for the given key before.
     * If no fetcher exists null will returned.
     *
     * @param importerId the id of the importer the RemoteSeriesDataFetcher was registered for
     * @return the RemoteSeriesDataFetcher instance or null.
     */
    public synchronized RemoteSeriesDataFetcher retrieveSeriesDataFetcher(String importerId) {

        if (!initializationComplete) {
            init();
        }

        return registeredDataFetcher.get(importerId);
    }

}
