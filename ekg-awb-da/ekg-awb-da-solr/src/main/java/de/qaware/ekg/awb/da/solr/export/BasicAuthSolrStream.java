package de.qaware.ekg.awb.da.solr.export;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.io.stream.JSONTupleStream;
import org.apache.solr.client.solrj.io.stream.JavabinTupleStreamParser;
import org.apache.solr.client.solrj.io.stream.SolrStream;
import org.apache.solr.client.solrj.io.stream.TupleStreamParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * A SolrStream instance that extends the standard Solr stream with BasicAuth support
 */
public class BasicAuthSolrStream extends SolrStream {

    private String basicAuthUsername;

    private String basicAuthPassword;

    private CloseableHttpResponse closeableHttpResponse;

    private SolrParams solrParams;

    private SolrClient solrClient;

    /**
     * Constructs a new instance of BasicAuthSolrStream that uses the
     * given parameters to connect to Solr server and fetch the data
     *
     * @param baseUrl Base URL of the stream.
     * @param params  Map&lt;String, String&gt; of parameters
     * @param username the username for BasicAuth authentication or null if no authentication required
     * @param password the password for BasicAuth authentication or null if no authentication required
     */
    public BasicAuthSolrStream(String baseUrl, SolrParams params, String username, String password) {
        super(baseUrl, params);
        this.basicAuthUsername = username;
        this.basicAuthPassword = password;
        this.solrParams = params;
    }

    public BasicAuthSolrStream(SolrClient solrClient, SolrParams params ) {
        super("", params);
        this.solrClient = solrClient;
        this.solrParams = params;
    }

    /**
     *  Closes the Stream to a single Solr Instance
     */
    @Override
    public void close() throws IOException {
        if (closeableHttpResponse != null) {
            closeableHttpResponse.close();
        }

        if (solrClient != null) {
            super.close();
        }
    }

    public void open() throws IOException {

        if (solrClient != null) {
            try {
                SolrParams params =  (SolrParams) MethodUtils.invokeExactMethod(this, "loadParams", solrParams);

                FieldUtils.writeField(this, "tupleStreamParser", constructParser(solrClient, params), true);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            super.open();
        }
    }

    // Note: In Solr 9, constructParser signature changed. This is now a custom method, not an override.
    private TupleStreamParser constructParser(SolrClient server, SolrParams requestParams)
            throws IOException, SolrServerException {

        String p = requestParams.get("qt");
        if (p != null) {
            ModifiableSolrParams modifiableSolrParams = (ModifiableSolrParams) requestParams;
            modifiableSolrParams.remove("qt");
            //performance optimization - remove extra whitespace by default when streaming
            modifiableSolrParams.set("indent", modifiableSolrParams.get("indent", "off"));
        }

        String wt = requestParams.get(CommonParams.WT, "json");

        QueryRequest query = new QueryRequest(requestParams);
        query.setPath(p);
        query.setResponseParser(new InputStreamResponseParser(wt));
        query.setMethod(SolrRequest.METHOD.POST);

        if (StringUtils.isNotBlank(basicAuthUsername)) {
            query.setBasicAuthCredentials(basicAuthPassword, basicAuthPassword);
        }

        NamedList<Object> genericResponse = server.request(query);
        InputStream stream = (InputStream) genericResponse.get("stream");
        this.closeableHttpResponse = (CloseableHttpResponse)genericResponse.get("closeableResponse");

        if (CommonParams.JAVABIN.equals(wt)) {
            return new JavabinTupleStreamParser(stream, false);
        } else {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return new JSONTupleStream(reader);
        }
    }
}
