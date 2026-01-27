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
package de.qaware.ekg.awb.repository.api;

import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;

import java.util.List;

/**
 * ConfigService manages the user specific configuration like connected types.
 */
public interface RepositoryService extends RepositoryClientAware {

    /**
     * Return a list with all configured EKG repositories.
     * Every types must not occur more than once.
     *
     * @return A list with the configured types.
     */
    List<EkgRepository> listEkgRepositories();

    /**
     * Removes a repository from Software-EKG workbench.
     * Call thees method will only delete the connection and description data of the repository not the
     * (remote) repository itself. The time series and other information's stored in the database keep untouched
     * and can relinked later.
     *
     * @param id the unique technical id of the repository in the embedded database
     */
    void deleteRepository(String id);

    /**
     * Removes a repository from Software-EKG workbench by using the {@link EkgRepository} instance directly.
     *
     * Call thees method will only delete the connection and description data of the repository not the
     * (remote) repository itself. The time series and other information's stored in the database keep untouched
     * and can relinked later.
     *
     * @param repository The types.
     */
    void deleteRepository(EkgRepository repository);

    /**
     * Add a new {@link EkgRepository} that will persisted in the embedded data store.
     * To add an repository mean to create a link to an external database the repository is based on.
     * Only the connection data, credentials and further metadata of the repository will stored in EKG, not
     * the payload the external database store itself.
     *
     * @param name the alias name of the repository shown in the UI
     * @param connectionUrl The uri to connect to the types.
     * @param indexName the name of the search index like the Solr Collection (Cloud) or the Solr Core (Standalone)
     * @param repositoryDbType the type of the database behind the repository like SOLR_EMBEDDED or ELASTICSEARCH_STANDALONE
     * @param resourceAuthType the type of authentication need to communicate with the repository (none, user-pass, API-Key)
     * @param username the username to use in case of authType = 'USERNAME_PASSWORD'
     * @param password the password to use in case of authType = 'USERNAME_PASSWORD'
     *
     * @return The created types instance.
     */
    EkgRepository addEkgRepository(String name, String connectionUrl, String indexName,
                                   EkgRepositoryDbType repositoryDbType,
                                   ResourceAuthType resourceAuthType,
                                   String username, String password);


}
