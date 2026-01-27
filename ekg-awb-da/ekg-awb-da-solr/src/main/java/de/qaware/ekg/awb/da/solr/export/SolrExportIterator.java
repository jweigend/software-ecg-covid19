package de.qaware.ekg.awb.da.solr.export;

import de.qaware.ekg.awb.da.solr.utils.LoggingUtils;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.CloudSolrStream;
import org.apache.solr.client.solrj.io.stream.SolrStream;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that executes a Solr query against the /export request handler and returns the matching documents
 * as entities in a streaming fashion.
 *
 * @param <T> matching documents will be converted to entities of this type
 */
/* package-private */ class SolrExportIterator<T> implements Iterator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrExportIterator.class);

    private final TupleObjectBinder tupleObjectBinder;
    private final BinderConfig<T> binderConfig;
    private final TupleStream tupleStream;
    private final long tsOpened;

    // Because the SolrStream has no next() or peek() functionality, read one Tuple ahead in this iterator.
    private Tuple nextTuple;

    /**
     * Constructs a {@link SolrExportIterator}.
     *
     * @param type          the type of the entities
     * @param collection    The Solr collection
     * @param solrParams    Parameters for the Solr query, e.g. filters
     * @param streamContext stream context containing a {@link org.apache.solr.client.solrj.impl.CloudSolrClient}
     *                      to be reused
     * @param zkHost        ZooKeeper hosts as comma-separated string (may be null). If this is set, we internally
     *                      use a {@link CloudSolrStream} - always use this for internal access from within the
     *                      cluster (e.g. from PSMG Hub tsquery)
     * @param baseUrl       The baseUrl of one Solr node (may be null). If this is set, we internally use a
     *                      {@link SolrStream} - use this for external access
     *                      from outside the cluster (e.g. from a test driver in module tests).
     * @param username      the username for BasicAuth authentication or null if no authentication required
     * @param password      the password for BasicAuth authentication or null if no authentication required
     *
     * @throws RepositoryException on errors opening the Solr stream
     */
    /* package-private */ SolrExportIterator(Class<T> type, String collection, SolrParams solrParams,
                                             StreamContext streamContext, String zkHost, String baseUrl,
                                             String username, String password) throws RepositoryException {

        this(type, collection, zkHost, solrParams, streamContext,
                createTupleStream(collection, solrParams, zkHost, baseUrl, username, password));
    }

    /* package-private */ SolrExportIterator(Class<T> type, SolrParams solrParams,
                                             StreamContext streamContext, SolrClient solrClient) throws RepositoryException {
        this(type, "-none-", "-none-", solrParams, streamContext, new BasicAuthSolrStream(solrClient, solrParams));
    }


    /**
     * Constructs a {@link SolrExportIterator}.
     *
     * @param type          the type of the entities
     * @param zkHost        ZooKeeper hosts as comma-separated string (may be null). If this is set, we internally
     *                      use a {@link CloudSolrStream} - always use this for internal access from within the
     *                      cluster (e.g. from PSMG Hub tsquery)
     * @param collection    The Solr collection
     * @param solrParams    Parameters for the Solr query, e.g. filters
     * @param streamContext stream context containing a {@link org.apache.solr.client.solrj.impl.CloudSolrClient} to be reused
     * @param tupleStream   The stream to process the response
     *
     * @throws RepositoryException on errors opening the Solr stream
     */
    /* package-private */ SolrExportIterator(Class<T> type, String collection, String zkHost, SolrParams solrParams,
                                             StreamContext streamContext, TupleStream tupleStream) throws RepositoryException {
        try {
            tupleObjectBinder = new TupleObjectBinder();
            binderConfig = tupleObjectBinder.getBinderConfig(type);

            if (LOGGER.isDebugEnabled()) {
                String paramsForLogging = LoggingUtils.getShortenedStringForLogging(solrParams);
                LOGGER.debug("Executing Solr export query: {}", paramsForLogging);
            }

            tsOpened = System.currentTimeMillis();
            this.tupleStream = tupleStream;
            tupleStream.setStreamContext(streamContext);
            tupleStream.open();
            nextTuple = tupleStream.read();
        } catch (IOException e) {
            throw new RepositoryException("Error opening SolrStream with zkHost=" + zkHost
                    + ", collection=" + collection + ", solrParams=" + solrParams, e);
        }
    }

    /**
     * Returns {@code true} if the result iterator has more elements; {@code false} otherwise. In case {@code false}
     * is returned, the underlying stream will be closed.
     *
     * @return {@code true} if the result iterator has more elements; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        boolean hasNext = !nextTuple.EOF;
        if (!hasNext) {
            try {
                tupleStream.close();

                if (LOGGER.isDebugEnabled()) {
                    long tsClosed = System.currentTimeMillis();
                    LOGGER.debug("Closed Solr export stream after {} ms", tsClosed - tsOpened);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error closing SolrStream", e);
            }
        }
        return hasNext;
    }

    /**
     * Reads and returns the next entity from the stream.
     *
     * @return the next entity
     */
    @Override
    public T next() {
        if (nextTuple.EOF) {
            throw new NoSuchElementException("End of stream reached");
        }

        try {
            T entity = tupleObjectBinder.getBean(binderConfig, nextTuple);
            nextTuple = tupleStream.read();
            return entity;
        } catch (IOException e) {
            throw new IllegalStateException("Error reading the next element", e);
        }
    }

    private static TupleStream createTupleStream(String collection, SolrParams solrParams, String zkHost,
                                                 String baseUrl, String username, String password) throws RepositoryException {
        try {
            if (!StringUtils.isBlank(zkHost)) {
                return new CloudSolrStream(zkHost, collection, solrParams);
            } else if (!StringUtils.isBlank(baseUrl)) {

                return new BasicAuthSolrStream(baseUrl + "/" + collection, solrParams, username, password);
            } else {
                throw new IllegalStateException("Neither zkHost nor baseUrl are set - can't create a TupleStream");
            }
        } catch (IOException e) {
            throw new RepositoryException("Error opening SolrStream with zkHost=" + zkHost
                    + ", collection=" + collection + ", solrParams=" + solrParams, e);
        }
    }
}
