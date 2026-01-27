package de.qaware.ekg.awb.repository.bl.mapper;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.bl.repositories.RepositoryEt;
import de.qaware.ekg.awb.sdk.awbapi.repository.ImporterSourceRepository;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import org.slf4j.Logger;

import java.util.List;

/**
 * Utils class for mapping RepositoryEt to EkgRepository and vice versa.
 */
public class RepositoryMapper {

    private static final Logger LOGGER = EkgLogger.get();

    private RepositoryMapper() {
    }

    /**
     * Constructor which will a SolrClassicRepository instance based
     * on the given repository data.
     *
     * @param repository the alias name of the repository shown in the UI
     * @param url        the connection string to the database the repository is based on (URL or Host:Port list)
     * @param indexName  the name of the search index like the Solr Collection (Cloud) or the Solr Core (Standalone)
     * @param type       the type of the database behind the repository like SOLR_EMBEDDED or ELASTICSEARCH_STANDALONE
     * @param authType   the type of authentication need to communicate with the repository (none, user-pass, API-Key)
     * @param username   the username to use in case of authType = 'USERNAME_PASSWORD'
     * @param password   the password to use in case of authType = 'USERNAME_PASSWORD'
     *
     * @return an RepositoryEt instance initialized with the specified data
     */
    public static RepositoryEt createRepositoryEt(String repository, String url, String indexName, EkgRepositoryDbType type,
                                                  ResourceAuthType authType, String username, String password) {
        RepositoryEt et = new RepositoryEt();

        et.setName(repository);
        et.setUrl(url);
        et.setDbIndexName(indexName);
        et.setAuthType(authType.toString());
        et.setUsername(username);
        et.setPassword(password);
        et.setRepositoryTypeString(type.name());

        return et;
    }

    /**
     * Build a concrete {@link EkgRepository} from a stored types entry.
     *
     * @param repositoryEntity The stored types entry.
     *
     * @return A concrete {@link EkgRepository} instance.
     */
    public static EkgRepository buildRepository(RepositoryEt repositoryEntity) {

        List<EkgRepository.Factory> repositoryFactories = EkgLookup.lookupAll(EkgRepository.Factory.class);

        if (repositoryFactories == null) {
            LOGGER.error("Unable to retrieve any EkgRepository.Factory from EKG application context.");
            return null;
        }

        for (EkgRepository.Factory factory : repositoryFactories) {
            if (factory.isTypeSupported(repositoryEntity.getRepositoryType())) {
                return factory.getInstance(
                        repositoryEntity.getId(),
                        repositoryEntity.getName(),
                        repositoryEntity.getUrl(),
                        repositoryEntity.getDbIndexName(),
                        EkgRepositoryDbType.valueOf(repositoryEntity.getRepositoryTypeString()),
                        ResourceAuthType.valueOf(repositoryEntity.getAuthType()),
                        repositoryEntity.getUsername(),
                        repositoryEntity.getPassword());
            }
        }

        return null;
    }

    public static RepositoryEt mapToRepositoryEt(ImporterSourceRepository sourceRepository) {
        RepositoryEt entity = new RepositoryEt();
        entity.setId(sourceRepository.getId());
        entity.setAccordingProjectName(sourceRepository.getAccordingProjectName());
        entity.setUrl(sourceRepository.getRepositoryUrl());
        entity.setAuthType(sourceRepository.getAuthType().toString());
        entity.setUsername(sourceRepository.getUsername());
        entity.setPassword(sourceRepository.getPassword());
        entity.setOauthServerUrl(sourceRepository.getOAuthServerUrl());
        entity.setSsoRefreshToken(sourceRepository.getSsoRefreshToken());

        return entity;
    }

    public static ImporterSourceRepository mapToImporterSourceRepository(RepositoryEt repositoryEt) {
        ImporterSourceRepository entity = new ImporterSourceRepository();
        entity.setId(repositoryEt.getId());
        entity.setAccordingProjectName(repositoryEt.getAccordingProjectName());
        entity.setRepositoryUrl(repositoryEt.getUrl());
        entity.setAuthType(ResourceAuthType.valueOf(repositoryEt.getAuthType()));
        entity.setUsername(repositoryEt.getUsername());
        entity.setPassword(repositoryEt.getPassword());
        entity.setOAuthServerUrl(repositoryEt.getOauthServerUrl());
        entity.setSsoRefreshToken(repositoryEt.getSsoRefreshToken());

        return entity;
    }
}
