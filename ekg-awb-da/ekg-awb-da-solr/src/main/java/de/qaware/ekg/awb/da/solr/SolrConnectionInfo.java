package de.qaware.ekg.awb.da.solr;

import java.util.Collection;

/**
 * Information required to create a connection to Solr.
 */
public interface SolrConnectionInfo {

    /**
     * Returns the ZooKeeper host(s) as collection. Example: ["host1:9983", "host2:9983"]
     *
     * @return the ZooKeeper host(s)
     */
    Collection<String> getZkHosts();

    /**
     * Returns the ZooKeeper host(s) as comma-separated String. Example: "host1:9983,host2:9983"
     *
     * @return the ZooKeeper host(s)
     */
    String getZkHostsString();

    /**
     * Returns the ZooKeeper client timeout.
     *
     * @return ZooKeeper client timeout
     */
    int getZkClientTimeout();

    /**
     * Returns the ZooKeeper connect timeout.
     *
     * @return ZooKeeper connect timeout
     */
    int getZkConnectTimeout();
}
