package de.qaware.ekg.awb.da.solr;

import de.qaware.ekg.awb.repository.api.SearchIndexAdminClient;
import de.qaware.ekg.awb.repository.api.dataobject.admin.CollectionProperties;
import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of {@link SearchIndexAdminClient} for Solr.
 */
public class SolrRepositoryAdminClient implements SearchIndexAdminClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrRepositoryAdminClient.class);

    /**
     * The prefix for shard names.
     */
    private static final String SHARDNAME_PREFIX = "shard";

    /**
     * Name of the property in the balance leaders request
     */
    private static final String PREFERRED_LEADER_PROPERTY = "preferredLeader";

    /**
     * Wrapper to execute a request with a SolrClient
     */
    private RequestProcessor requestProcessor;

    /**
     * Constructor
     *
     * @param solrClient a SolrConnection; must be created without a binding to a particular collection.
     */
    /* package private */ SolrRepositoryAdminClient(SolrClient solrClient) {
        this(new RequestProcessor(solrClient));
    }

    /* package-private */ SolrRepositoryAdminClient(RequestProcessor requestProcessor) {
        Objects.requireNonNull(requestProcessor, "RequestProcessor must not be null.");
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void setAlias(String alias, String collectionName) throws RepositoryException {
        requestProcessor.process(CollectionAdminRequest.createAlias(alias, collectionName));
        // The response from Solr is empty, thus response.isSuccess() is always false
    }

    @Override
    public void deleteAlias(String alias) throws RepositoryException {
        requestProcessor.process(CollectionAdminRequest.deleteAlias(alias));
        // The response from Solr is empty, thus response.isSuccess() is always false
    }

    @Override
    public void create(CollectionProperties collectionProperties) throws RepositoryException {
        int numRetries = 3;
        int secondsBetweenRetries = 10;
        while (true) {
            --numRetries;

            CollectionAdminResponse response = createCollection(collectionProperties);

            if (response.isSuccess() &&
                    (response.getErrorMessages() == null || response.getErrorMessages().size() == 0)) {
                return; // Collection was created successfully
            }

            if (numRetries <= 0) {
                throw new RepositoryException("Failed to create collection "
                        + collectionProperties.getCollectionName() + ": " + response.toString()
                );
            }

            LOGGER.warn("Could not create collection {}. Retrying in {} seconds. {} retries left.",
                    collectionProperties.getCollectionName(), secondsBetweenRetries, numRetries);

            try {
                Thread.sleep(secondsBetweenRetries * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private CollectionAdminResponse createCollection(CollectionProperties collectionProperties) throws RepositoryException {
        final int nodesInCluster = getNodesInCluster();
        final int replicas = Math.min(nodesInCluster, collectionProperties.getAmountOfReplicas());
        if (replicas < collectionProperties.getAmountOfReplicas()) {
            LOGGER.warn("Cannot create {} replicas because there are only {} nodes in the cluster",
                    collectionProperties.getAmountOfReplicas(), nodesInCluster
            );
        }

        LOGGER.info("Creating collection {} with {} shards and {} replicas.",
                collectionProperties.getCollectionName(),
                collectionProperties.getAmountOfShards(),
                replicas
        );

        CollectionAdminRequest.Create createRequest =
                CollectionAdminRequest
                        .createCollection(
                                collectionProperties.getCollectionName(),
                                collectionProperties.getConfigIdentifier(),
                                collectionProperties.getAmountOfShards(),
                                replicas);
                        // Note: setMaxShardsPerNode was removed in Solr 9 - shard distribution is now automatic

        if (collectionProperties.getRouterField().isPresent() && collectionProperties.getAmountOfShards() > 1) {
            createRequest.setRouterField(collectionProperties.getRouterField().get());
            createRequest.setShards(getShardNamesCommaSeparated(collectionProperties));
        }

        CollectionAdminResponse response = requestProcessor.process(createRequest);
        logResponse(response);
        return response;
    }

    @Override
    public void rebalanceShardLeaders(String collectionName) throws RepositoryException {
        LOGGER.info("Rebalancing shard leaders of collection {}", collectionName);

        // Distribute preferred leader property evenly: ---------------------------------
        CollectionAdminRequest.BalanceShardUnique balanceShardUnique = CollectionAdminRequest.balanceReplicaProperty(
                collectionName, PREFERRED_LEADER_PROPERTY);

        CollectionAdminResponse balanceResponse = requestProcessor.process(balanceShardUnique);
        LOGGER.info("Solr response for BALANCESHARDUNIQUE: {}", balanceResponse);

        // Balance response will only contain 0 as status. This check is here only to be 100% save:
        if (balanceResponse.getStatus() != 0) {
            throw new RepositoryException("Failed to balance preferred leaders property: " + balanceResponse
                    .toString());
        }

        // We need to wait a bit. If the REBALANCELEADERS request is sent directly after the BALANCESHARDUNIQUE
        // request, Solr will not rebalance the Shards.
        timedWaitForSolr();

        // Init leader election: ---------------------------------------------------------
        CollectionAdminRequest.RebalanceLeaders rebalanceLeaders = CollectionAdminRequest.rebalanceLeaders(
                collectionName);

        CollectionAdminResponse rebalanceLeadersResponse = requestProcessor.process(rebalanceLeaders);
        LOGGER.info("Solr response for REBALANCELEADERS: {}", rebalanceLeadersResponse);

        // Balance response will only contain 0 as status. Since Solr does not give us any error messages at all there
        // is no way to check if the request was successful. This check is here only to be 100% save:
        if (rebalanceLeadersResponse.getStatus() != 0) {
            throw new RepositoryException("Failed to start leader election: " + rebalanceLeadersResponse.toString());
        }

        // We need to wait a bit. If the collection is being updated directly after the REBALANCELEADERS
        // request, Solr may fail due to the following error:
        //
        // 11.12.2017 04:21:41.286 [1bc6d88a-6cb9-4022-b8f8-1a7d421a7839] [SOLRINTERN] [qtp407858146-330999]
        // ERROR org.apache.solr.handler.RequestHandlerBase - org.apache
        //         .solr.common.SolrException: ClusterState says we are the leader (
        //),
        // but locally we don't think so. Request came from null
        // at org.apache.solr.update.processor.DistributedUpdateProcessor
        // .doDefensiveChecks(DistributedUpdateProcessor.java:658)
        // at org.apache.solr.update.processor.DistributedUpdateProcessor
        // .setupRequest(DistributedUpdateProcessor.java:418)
        // at org.apache.solr.update.processor.DistributedUpdateProcessor
        // .setupRequest(DistributedUpdateProcessor.java:346)
        // at org.apache.solr.update.processor.DistributedUpdateProcessor
        // .processAdd(DistributedUpdateProcessor.java:703)
        //
        // See also the solr bug tracker: https://issues.apache.org/jira/browse/SOLR-11685?
        timedWaitForSolr();
    }

    /**
     * Solr doesn't seem to guarantee that the previous admin command has actually completed before returning.
     * Hence we do a timed wait here.
     */
    private void timedWaitForSolr() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for Solr", e);
            // Restore the interrupted flag to true
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void delete(String collectionName) throws RepositoryException {
        CollectionAdminResponse response = requestProcessor.process(CollectionAdminRequest.deleteCollection
                (collectionName));
        if (!response.isSuccess()) {
            throw new RepositoryException(
                    "Failed to delete collection " + collectionName + ": " + response.toString()
            );
        }
    }

    @Override
    public String getCollectionNameForAlias(String alias) throws RepositoryException {
        CollectionAdminResponse response = requestProcessor.process(CollectionAdminRequest.getClusterStatus());
        logResponse(response);

        NamedList<Object> clusterMap = getClusterSection(response);
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) clusterMap.get("aliases");
        return map == null ? null : map.get(alias);
    }

    @Override
    public Collection<CollectionProperties> listCollections() throws RepositoryException {
        CollectionAdminResponse response = requestProcessor.process(CollectionAdminRequest.getClusterStatus());
        logResponse(response);

        NamedList<Object> clusterMap = getClusterSection(response);
        @SuppressWarnings("unchecked")
        NamedList<Object> collectionMap = (NamedList<Object>) clusterMap.get("collections");
        if (collectionMap == null) {
            collectionMap = new NamedList<>();
        }

        Collection<CollectionProperties> collectionDefinitions = new ArrayList<>(collectionMap.size());

        for (Map.Entry<String, Object> reproDataEntry : collectionMap) {
            CollectionProperties collectionDefinition = new CollectionProperties();
            collectionDefinition.setCollectionName(reproDataEntry.getKey());

            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) reproDataEntry.getValue();
            @SuppressWarnings("unchecked")
            List<String> aliases = (List<String>) props.get("aliases");

            if (aliases != null) {
                if (aliases.size() > 1) {
                    throw new RepositoryException("multiple aliases are assigned to Solr collection " +
                            reproDataEntry.getKey());
                }

                if (aliases.size() == 1) {
                    collectionDefinition.setCollectionAlias(aliases.get(0));
                }
            }

            collectionDefinition.setAmountOfShards(Integer.valueOf(props.get("maxShardsPerNode").toString()));
            collectionDefinition.setAmountOfReplicas(Integer.valueOf(props.get("replicationFactor").toString()));
            collectionDefinition.setConfigIdentifier((String) props.get("configName"));

            collectionDefinitions.add(collectionDefinition);
        }

        return collectionDefinitions;
    }

    @Override
    public void close() {
        // No op in this implementation because we use a singleton CloudSolrClient passed in from outside.
        // The interface still implements Closeable so that we can change the client usage without outside changes.
    }

    private int getNodesInCluster() throws RepositoryException {
        CollectionAdminResponse response = requestProcessor.process(CollectionAdminRequest.getClusterStatus());
        logResponse(response);

        NamedList<Object> clusterMap = getClusterSection(response);
        @SuppressWarnings("unchecked")
        List<Object> nodeList = (List<Object>) clusterMap.get("live_nodes");
        return nodeList == null ? 1 : nodeList.size();
    }

    private void logResponse(CollectionAdminResponse response) {
        LOGGER.trace("Cluster status response: {}", response);
    }

    @SuppressWarnings("unchecked")
    private NamedList<Object> getClusterSection(CollectionAdminResponse response) {
        return (NamedList<Object>) response.getResponse().get("cluster");
    }

    /**
     * Determines the names of the shards for the given collection definition.
     *
     * @return Comma separated string with shard names
     */
    private String getShardNamesCommaSeparated(CollectionProperties collectionProperties) {
        return IntStream.rangeClosed(0, collectionProperties.getAmountOfShards() - 1)
                .mapToObj(i -> SHARDNAME_PREFIX + Integer.toString(i))
                .collect(Collectors.joining(","));
    }

    /**
     * Wrapper class to execute a request with a SolrClient. Used in unit tests.
     */
    // Class must not be final to allow testing with Mockito mocks
    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.design.FinalClassCheck")
    /* package-private */ static class RequestProcessor {
        private SolrClient client;

        private RequestProcessor(SolrClient client) {
            Objects.requireNonNull(client, "Solr client must not be null.");
            this.client = client;
        }

        /**
         * Executes the given Solr request.
         *
         * @param req the Solr request to execute
         * @param <T> the type of the Solr response
         * @return the Solr response
         * @throws RepositoryException thrown in case of an IO error or an exception from Solr
         */
        public <T extends SolrResponse> T process(SolrRequest<T> req) throws RepositoryException {
            try {
                return req.process(client);
            } catch (SolrServerException | IOException e) {
                throw new RepositoryException(e);
            }
        }
    }
}
