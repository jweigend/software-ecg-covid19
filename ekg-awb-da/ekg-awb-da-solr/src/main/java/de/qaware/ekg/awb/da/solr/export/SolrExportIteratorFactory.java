package de.qaware.ekg.awb.da.solr.export;

import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.common.params.SolrParams;

import java.util.Iterator;

/**
 * Factory to create {@link SolrExportIterator}s.
 */
public class SolrExportIteratorFactory {

    /**
     * Creates a {@link SolrExportIterator}.
     *
     * @param <T>           the type of the entities
     * @param collection    The Solr collection
     * @param type          the type of the entities
     * @param solrParams    Parameters for the Solr query, e.g. filters
     * @param streamContext stream context containing a {@link org.apache.solr.client.solrj.impl.CloudSolrClient}
     *                      to be reused
     * @param zkHost        ZooKeeper hosts as comma-separated string (may be null). If this is set, we internally
     *                      use a {@link org.apache.solr.client.solrj.io.stream.CloudSolrStream} - always use this
     *                      for internal access from within the cluster (e.g. from PSMG Hub tsquery)
     * @param baseUrl       The baseUrl of one Solr node (may be null). If this is set, we internally use a
     *                      {@link org.apache.solr.client.solrj.io.stream.SolrStream} - use this for external access
     *                      from outside the cluster (e.g. from a test driver in module tests).
     * @param username      the username for BasicAuth authentication or null if no authentication required
     * @param password      the password for BasicAuth authentication or null if no authentication required
     *
     * @return the {@link SolrExportIterator}
     * @throws RepositoryException on errors opening the Solr stream
     */
    public <T> Iterator<T> create(String collection, Class<T> type, SolrParams solrParams, StreamContext streamContext,
                                  String zkHost, String baseUrl, String username, String password) throws RepositoryException {
        return new SolrExportIterator<>(type, collection, solrParams, streamContext, zkHost, baseUrl, username, password);
    }

    public <T> Iterator<T> create(SolrClient solrClient, Class<T> type, SolrParams solrParams,
                                  StreamContext streamContext) throws RepositoryException {
        return new SolrExportIterator<>(type, solrParams, streamContext, solrClient);
    }
}
