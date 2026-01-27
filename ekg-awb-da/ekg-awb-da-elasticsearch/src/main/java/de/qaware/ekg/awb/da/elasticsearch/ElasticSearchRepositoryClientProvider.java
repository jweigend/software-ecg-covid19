package de.qaware.ekg.awb.da.elasticsearch;

import de.qaware.ekg.awb.repository.api.RepositoryClient;
import de.qaware.ekg.awb.repository.api.RepositoryClientFactory;
import de.qaware.ekg.awb.repository.api.types.ElasticSearch;
import de.qaware.ekg.awb.repository.api.types.Remote;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A provider that acts as factory for RepositoryClient instance based on
 * the ElasticSearch RestHighLevelClient and for the RestHighLevelClient itself.
 */
@Remote
@ElasticSearch
public class ElasticSearchRepositoryClientProvider implements RepositoryClientFactory {

    private RestHighLevelClient elasticSearchClient;


    //================================================================================================================
    //  implementation of RepositoryClientFactory interface
    //================================================================================================================

    @Override
    public RepositoryClient createRepositoryClient(String connectionUrl, String userName, String password) {
        return new ElasticSearchRepositoryClient(getClient(connectionUrl, userName, password),
                extractDbIndexName(connectionUrl));
    }

    @Override
    public RepositoryClient createRepositoryClient(String clusterManagerUrl, String repositoryName,
                                                   String userName, String password) {
        throw new NotImplementedException("ElasticSearch doesn't use clients that connect via dedicated cluster manager");
    }

    public RestHighLevelClient getClient(String connectionUrl, String userName, String password) {
        try {

            if (elasticSearchClient == null) {
                URL url = new URL(connectionUrl);
                HttpHost elasticSearchHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());

                RestClientBuilder builder;

                if (StringUtils.isNotBlank(userName) ) {
                    builder = getSecureClientBuilder(elasticSearchHost, userName, password);
                } else {
                    builder = RestClient.builder(elasticSearchHost);
                }

                this.elasticSearchClient = new RestHighLevelClient(builder);
            }

        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        return elasticSearchClient;
    }

    private static String extractDbIndexName(String connectionUrl) {

        if (StringUtils.isBlank(connectionUrl)) {
            return "";
        }

        String[] uriTokes = connectionUrl.replaceAll("/+$","").split("/");

        return uriTokes[uriTokes.length - 1];
    }

    private RestClientBuilder getSecureClientBuilder(HttpHost elasticSearchHost, String username, String password) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return RestClient.builder(elasticSearchHost).setHttpClientConfigCallback(
                        httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    }
}
