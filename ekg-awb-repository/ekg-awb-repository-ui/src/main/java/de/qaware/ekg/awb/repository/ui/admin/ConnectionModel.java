//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.repository.ui.admin;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;

/**
 * Data model for the connection dialog.
 */
public class ConnectionModel {

    /**
     * The unique name of the types
     */
    private StringProperty repositoryName = new SimpleStringProperty();

    /**
     * The connection URL of the types
     * (host + port or complete URL with URI parts depending on types type)
     */
    private StringProperty repositoryConnectionUrl = new SimpleStringProperty();

    /**
     * The name of the database index.
     * In Solr speech this will be the collection at Solr cloud or the core at Solr classic
     */
    private StringProperty dbIndexName = new SimpleStringProperty();

    /**
     * The type of the types
     * (Solr Cloud, Solr Classic Standalone or ElasticSearch)
     */
    private ObjectProperty<EkgRepositoryDbType> repositoryDbType = new SimpleObjectProperty<>();

    /**
     * The authentication type the types used
     * for client connections
     */
    private ObjectProperty<ResourceAuthType> repositoryAuthType = new SimpleObjectProperty<>();

    /**
     * The username used for authentication (optional)
     */
    private StringProperty repositoryAuthUsername = new SimpleStringProperty();

    /**
     * The password used for authentication (optional)
     */
    private StringProperty repositoryAuthPassword = new SimpleStringProperty();

    /**
     * The EKG types before it get modified
     * (filled in case of types modification actions)
     */
    private ObjectProperty<EkgRepository> oldRepository = new SimpleObjectProperty<>();

    /**
     * a boolean property that indicates if the textfield with the types name is valid or not
     */
    private BooleanProperty repositoryNameIsValid = new SimpleBooleanProperty(false);

    /**
     * a boolean property that indicates if the textfield with the types connection url is valid or not
     */
    private BooleanProperty repositoryConnectionUrlIsValid = new SimpleBooleanProperty(false);

    private BooleanProperty isEditingMode = new SimpleBooleanProperty(false);


    //================================================================================================================
    //  model API
    //================================================================================================================


    /**
     * Initialize a new connection model.
     */
    public ConnectionModel() {
        oldRepository.isNotNull().addListener((o, ov, nv) -> {
            EkgRepository repositoryToUpdate = oldRepository.get();

            if (repositoryToUpdate == null) {
                return;
            }

            repositoryDbType.set(repositoryToUpdate.getEkgRepositoryDbType());
            repositoryName.set(repositoryToUpdate.getRepositoryName());
            repositoryConnectionUrl.set(repositoryToUpdate.getConnectionUrl());
        });
    }

    /**
     * Checks whether a connection can be opened
     *
     * @return the binding for the flag
     */
    public BooleanBinding canOpenConnection() {
        return repositoryName.isNotEmpty()
                .and(repositoryConnectionUrl.isNotNull())
                .and(repositoryDbTypeProperty().isNotNull());
    }

    /**
     * Get the connection name
     *
     * @return the connection name.
     */
    public String getRepositoryName() {
        return repositoryName.get();
    }

    /**
     * Getter for the Property repositoryName.
     *
     * @return the repositoryName
     */
    public StringProperty repositoryNameProperty() {
        return repositoryName;
    }

    /**
     * Get the connection url
     *
     * @return the connection url
     */
    public String getRepositoryConnectionUrl() {
        return repositoryConnectionUrl.get();
    }

    /**
     * Getter for the Property repositoryConnectionUrl.
     *
     * @return the repositoryConnectionUrl
     */
    public StringProperty repositoryConnectionUrlProperty() {
        return repositoryConnectionUrl;
    }

    /**
     * Get the type of the types
     *
     * @return the types type.
     */
    public EkgRepositoryDbType getRepositoryDbType() {
        return repositoryDbType.get();
    }

    /**
     * Getter for the ObjectProtperty repositoryDbType.
     *
     * @return the repositoryDbType
     */
    public ObjectProperty<EkgRepositoryDbType> repositoryDbTypeProperty() {
        return repositoryDbType;
    }

    /**
     * @return the types to update
     */
    public EkgRepository getOldRepository() {
        return oldRepository.get();
    }

    /**
     * @return the property of types to update
     */
    public ObjectProperty<EkgRepository> oldRepositoryProperty() {
        return oldRepository;
    }

    /**
     * @param oldRepository the types to update
     */
    public void setOldRepository(EkgRepository oldRepository) {
        this.oldRepository.set(oldRepository);
        this.repositoryName.set(oldRepository.getRepositoryName());
        this.repositoryConnectionUrl.set(oldRepository.getConnectionUrl());
        this.dbIndexName.set(oldRepository.getDBIndexName());
        this.repositoryAuthType.set(oldRepository.getRepositoryAuthType());
        this.repositoryAuthUsername.set(oldRepository.getUsername());
        this.repositoryAuthPassword.set(oldRepository.getPassword());
    }

    public Property<ResourceAuthType> repositoryAuthTypeProperty() {
        return repositoryAuthType;
    }

    public ResourceAuthType getRepositoryAuthType() {
        return repositoryAuthType.get();
    }

    public String getRepositoryAuthUsername() {
        return repositoryAuthUsername.get();
    }

    public StringProperty repositoryAuthUsernameProperty() {
        return repositoryAuthUsername;
    }

    public String getRepositoryAuthPassword() {
        return repositoryAuthPassword.get();
    }

    public StringProperty repositoryAuthPasswordProperty() {
        return repositoryAuthPassword;
    }

    public BooleanProperty repositoryNameIsValidProperty() {
        return repositoryNameIsValid;
    }

    public BooleanProperty repositoryConnectionUrlIsValidProperty() {
        return repositoryConnectionUrlIsValid;
    }

    public void setRepositoryNameIsValid(boolean repositoryNameIsValid) {
        this.repositoryNameIsValid.set(repositoryNameIsValid);
    }

    public void setRepositoryConnectionUrlIsValid(boolean repositoryConnectionUrlIsValid) {
        this.repositoryConnectionUrlIsValid.set(repositoryConnectionUrlIsValid);
    }

    public String getDbIndexName() {
        return dbIndexName.get();
    }

    public StringProperty dbIndexNameProperty() {
        return dbIndexName;
    }

    public void setDbIndexName(String dbIndexName) {
        this.dbIndexName.set(dbIndexName);
    }

    public boolean isEditingMode() {
        return isEditingMode.get();
    }

    public BooleanProperty isEditingModeProperty() {
        return isEditingMode;
    }

    public void setIsEditingMode(boolean isEditingMode) {
        this.isEditingMode.set(isEditingMode);
    }
}
