package de.qaware.ekg.awb.da.solr.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.solr.client.solrj.io.SolrClientCache;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Factory for {@link SolrClientCache}s.
 */
public class SolrClientCacheFactory {

    /**
     * Creates and returns a new {@link SolrClientCache}.
     *
     * @return the {@link SolrClientCache}.
     */
    public SolrClientCache create() {
        return new SolrClientCache(buildAnyCertAcceptingHttpClient());
    }

    //===============================================================================================================
    // internal helpers
    //===============================================================================================================

    /**
     * Factory method that builds a new Apache HttpClient that accept
     * any SSL certificate used by the remote independent if self signed or whatever.
     */
    private HttpClient buildAnyCertAcceptingHttpClient() {

        return HttpClients
                .custom()
                .setSSLSocketFactory(getAcceptAllSLLFactory())
                .build();
    }

    /**
     * Creates a SSL socket factory that will wont check either the remote host names
     * nor the signing of the host certificates.
     * This is not secure but required if we don't want to import the certificates
     * of each environment.
     *
     * @return a SSLConnectionSocketFactory instance that won't check any of the SSL secure mechanisms
     */
    private SSLConnectionSocketFactory getAcceptAllSLLFactory()  {

        try {

            // use the TrustSelfSignedStrategy to allow Self Signed Certificates
            SSLContext sslContext = SSLContextBuilder
                    .create()
                    .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
                    .build();

            // we can optionally disable hostname verification.
            // if you don't want to further weaken the security, you don't have to include this.
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

            // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
            // and allow all hosts verifier.
            return new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }



}
