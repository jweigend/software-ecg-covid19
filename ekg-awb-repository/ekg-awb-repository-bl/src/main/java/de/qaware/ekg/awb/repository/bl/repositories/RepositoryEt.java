package de.qaware.ekg.awb.repository.bl.repositories;

import de.qaware.ekg.awb.repository.api.model.AbstractEt;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.api.schema.DocumentType;
import de.qaware.ekg.awb.repository.api.schema.PersistedField;
import org.apache.commons.lang3.StringUtils;

import static de.qaware.ekg.awb.repository.api.schema.EkgSchemaField.*;

/**
 * todo insert comment here
 */
public class RepositoryEt extends AbstractEt {


    @PersistedField(REPOSITORY_IS_IMPORTSOURCE)
    private boolean isImportSource;

    @PersistedField(REPOSITORY_NAME)
    private String name;

    @PersistedField(REPOSITORY_ACCORDING_PROJECT)
    private String accordingProjectName;

    @PersistedField(REPOSITORY_URL)
    private String url;

    @PersistedField(REPOSITORY_INDEX_NAME)
    private String dbIndexName;

    @PersistedField(REPOSITORY_DB_TYPE)
    private String dbType;

    @PersistedField(REPOSITORY_AUTH_TYPE)
    private String authType;

    @PersistedField(REPOSITORY_USER)
    private String username;

    @PersistedField(REPOSITORY_PASSWORD)
    private String password;

    @PersistedField(REPOSITORY_OAUTH_SERVER_URL)
    private String oauthServerUrl;

    @PersistedField(REPOSITORY_SSO_TOKEN)
    private String ssoRefreshToken;



    /**
     * Constructs a new instance of RepositoryEt
     */
    public RepositoryEt() {
        setType(DocumentType.REPOSITORY.name());
        isImportSource = true;
    }

    /**
     * Returns the unique id of the types
     *
     * @return  the unique id of the types
     */
    public String isId() {
        return super.getId();
    }

    /**
     * Sets the unique id of the types
     *
     * @param id  the unique id of the types
     */
    public void setId(String id) {
        super.setId(id);
    }

    /**
     * Returns the name of the types
     *
     * @return the name of the types
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of  the name of the types
     *
     * @param name  the name of the types
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return a boolean flag if the types is an import source
     *
     * @return true if the types is used by importers, false if is an EKG types
     */
    public boolean isImportSource() {
        return isImportSource;
    }

    /**
     * Sets a boolean flag if the types is an import source
     *
     * @param importSource true if the types is used by importers, false if is an EKG types
     */
    public void setImportSource(boolean importSource) {
        isImportSource = importSource;
    }

    /**
     * Get the URL of this types.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the URL of this types.
     *
     * @param uri the url of this types.
     */
    public void setUrl(String uri) {
        this.url = uri;
    }

    public String getDbIndexName(){
        return dbIndexName;
    }

    public void setDbIndexName(String dbIndexName){
        this.dbIndexName = dbIndexName;
    }

    /**
     * Get the type of this types.
     *
     * @return the types type.
     */
    public String getRepositoryTypeString() {
        return dbType;
    }

    public EkgRepositoryDbType getRepositoryType() {
        return StringUtils.isNotBlank(dbType) ? EkgRepositoryDbType.valueOf(dbType) : null;
    }

    /**
     * Set the types type.
     *
     * @param repositoryType the new types type.
     */
    public void setRepositoryTypeString(String repositoryType) {
        this.dbType = repositoryType;
    }

    public String getAccordingProjectName() {
        return accordingProjectName;
    }

    public void setAccordingProjectName(String projectName) {
        this.accordingProjectName = projectName;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOauthServerUrl() {
        return oauthServerUrl;
    }

    public void setOauthServerUrl(String oauthServerUrl) {
        this.oauthServerUrl = oauthServerUrl;
    }

    public String getSsoRefreshToken() {
        return ssoRefreshToken;
    }

    public void setSsoRefreshToken(String ssoRefreshToken) {
        this.ssoRefreshToken = ssoRefreshToken;
    }

    @Override
    public String toString() {
        return "RepositoryEt{" +
                "isImportSource=" + isImportSource +
                ", name='" + name + '\'' +
                ", accordingProjectName='" + accordingProjectName + '\'' +
                ", url='" + url + '\'' +
                ", dbIndexName='" + dbIndexName + '\'' +
                ", dbType='" + dbType + '\'' +
                ", authType='" + authType + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", oauthServer='" + oauthServerUrl + '\'' +
                ", ssoRefreshToken='" + ssoRefreshToken + '\'' +
                '}';
    }


}
