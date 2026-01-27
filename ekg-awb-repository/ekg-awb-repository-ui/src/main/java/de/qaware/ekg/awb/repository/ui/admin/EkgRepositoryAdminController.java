package de.qaware.ekg.awb.repository.ui.admin;

import de.qaware.ekg.awb.common.ui.bindings.StaticBoolProperty;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.RepositoryService;
import de.qaware.ekg.awb.repository.api.events.RepositoryModifyEvent;
import de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType;
import de.qaware.ekg.awb.repository.api.types.Embedded;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.importer.ui.skins.EkgComboBoxListViewSkin;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static de.qaware.ekg.awb.repository.api.model.EkgRepositoryDbType.*;
import static de.qaware.ekg.awb.sdk.awbapi.repository.ResourceAuthType.*;
import static de.qaware.ekg.awb.sdk.importer.ui.skins.EkgComboBoxListViewSkin.CellBehaviorResource;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

/**
 * The controller that manages the view that is used to specify or modify EKG repositories.
 * Managing means, input validation, write setting as apply action and control view states
 * like disable/enable states.
 */
public class EkgRepositoryAdminController implements Initializable {

    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    // --------------------------- validators for connection URLs / connection-strings ------------------------------

    private static final UrlValidator REPOSITORY_URL_VALIDATOR =
            new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);

    private static final String SINGLE_HOST_PATTERN
            = "((([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)*([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])(:[1-9][0-9]{1,4})?)";

    private static final String SINGLE_HOST_CAIN_PATTERN = "(," + SINGLE_HOST_PATTERN + ")*";

    private static final Pattern ZK_HOSTS_VALIDATION_PATTERN
            = Pattern.compile("^" + SINGLE_HOST_PATTERN + SINGLE_HOST_CAIN_PATTERN + "$", Pattern.CASE_INSENSITIVE);


    // ------------------------------------ style and behaviour definitions ------------------------------------------

    /**
     * per value icons and disable states for the ComboBox 'EkgRepositoryDbType'
     */
    private final Map<EkgRepositoryDbType, CellBehaviorResource> dbTypeResourceMap = Map.of(
            SOLR_EMBEDDED,
            new CellBehaviorResource("icons/repository-db-type-solr.svg", StaticBoolProperty.TRUE),

            SOLR_STANDALONE,
            new CellBehaviorResource("icons/repository-db-type-solr.svg", StaticBoolProperty.FALSE),

            SOLR_CLOUD,
            new CellBehaviorResource("icons/repository-db-type-solr-cloud.svg", StaticBoolProperty.FALSE),

            ELASTICSEARCH_STANDALONE,
            new CellBehaviorResource("icons/repository-db-type-elasticsearch.svg", StaticBoolProperty.FALSE)
    );

    /**
     * per value icons and disable states for the ComboBox 'EkgRepositoryDbType'
     */
    private final Map<ResourceAuthType, CellBehaviorResource> authTypeResourceMap = Map.of(
            NONE,
            new CellBehaviorResource("icons/repository-authtype-none-icon.svg", StaticBoolProperty.FALSE),

            USERNAME_PASSWORD,
            new CellBehaviorResource("icons/repository-authtype-password-icon.svg", StaticBoolProperty.FALSE),

            API_KEY,
            new CellBehaviorResource("icons/repository-authtype-apikey-icon.svg", StaticBoolProperty.TRUE),

            JWT_BEARER_TOKEN,
            new CellBehaviorResource("icons/repository-authtype-jwt-icon.svg", StaticBoolProperty.TRUE),

            OAUTH2_CLIENT_CREDENTIAL,
            new CellBehaviorResource("icons/repository-authtype-oauth2-icon.svg", StaticBoolProperty.TRUE),

            CERTIFICATE,
            new CellBehaviorResource("icons/repository-authtype-apikey-icon.svg", StaticBoolProperty.TRUE)
    );


    // ----------------------------------------- references to parent ------------------------------------------------

    /**
     * The dialog window that will display the administration view
     * this controller manages
     */
    private Dialog repositoryAdminDialog;

    /**
     * The dialog pane as child of the dialog that layouts the view and
     * provided all base control including the apply/cancel buttons
     */
    @FXML
    private DialogPane repositoryAdminDialogPane;


    // ----------------------------------- input fields to specify the types data ------------------------------------

    /**
     * The type of the types
     * (Solr Cloud, Solr Classic Standalone or ElasticSearch)
     */
    @FXML
    private ComboBox<EkgRepositoryDbType> cbRepositoryDbType;

    /**
     * The unique name of the types
     */
    @FXML
    private TextField txtRepositoryName;

    /**
     * The label of the url field that's store the connection URL of the types
     * (host + port or complete URL with URI parts depending on types type)
     */
    @FXML
    private Label lbRepositoryUrl;

    /**
     * The connection URL of the types
     * (host + port or complete URL with URI parts depending on types type)
     */
    @FXML
    private TextField txtRepositoryUrl;

    /**
     * The label of the index name field that's enable/disable state will
     * manged by the controller
     */
    @FXML
    private Label lbRepositoryIndexName;

    /*
     * The name of the types index
     * As example at Solr this is the collection or core
     * name depending if cloud or standalone mode.
     */
    @FXML
    private TextField txtRepositoryIndexName;

    /**
     * The authentication type the types used
     * for client connections
     */
    @FXML
    private ComboBox<ResourceAuthType> cbRepositoryAuthType;

    /**
     * The username used for authentication (optional)
     */
    @FXML
    private TextField txtRepositoryAuthUsername;

    /**
     * The password used for authentication (optional)
     */
    @FXML
    private PasswordField txtRepositoryAuthPassword;


    // -------- internal types and event bus used to read/write config data and publish it to others -----------------

    /**
     * EKG AWB internal types used to write the types
     * configuration and to check for duplicates
     */
    @Inject
    @Embedded
    private EkgRepository repository;

    /**
     * EKG event bus used to notify other about new
     * repositories or types modifications
     */
    @Inject
    private EkgEventBus eventBus;

    /**
     * the view model that stores the states/values of each input component
     */
    private ConnectionModel viewModel = new ConnectionModel();

    /**
     * a set that contains all known types names currently configured
     */
    private Set<String> knownRepositoryNames = new HashSet<>();

    //================================================================================================================
    //  Initializable interface implementation
    //================================================================================================================

    /* (non-Javadoc)
     * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // init pseudo classes and other view styles that
        // will setup programmatically
        initViewStyles();

        // init default behaviour (validation, filter, ...0) and binding
        // before calling methods overwritten by deriving classes
        initViewBehavior();

        // update view from model properties
        initModelToView();

        // init event handlers
        initHandler();
    }

    //================================================================================================================
    //  Controller API that could be used by the FileImportDialog component or deriving implementations
    //================================================================================================================

    /**
     * Sets the Dialog this controller belongs to an manages the
     * child components.
     *
     * @param repositoryAdminDialog the Dialog that own the components controlled by this controller
     */
    protected void setParent(Dialog repositoryAdminDialog) {
        this.repositoryAdminDialog = repositoryAdminDialog;
    }

    /**
     * Process a types modification request communicate
     * by another component. In this case this component will shown
     * to provide types settings.
     *
     * @param event the event that communicates a types modification and where it happens.
     * @return TRUE if
     */
    public boolean handleModifyRepositoryEvent(RepositoryModifyEvent event) {

        if (!event.isModificationRequested()) {
            return false;
        }

        viewModel.setIsEditingMode(true);
        repositoryAdminDialog.setTitle("Update EkgRepository");
        repositoryAdminDialog.initOwner(event.getOwnerWindow());
        setOldRepository(event.getRepository());

        repositoryAdminDialog.show();

        return true;
    }

    /**
     * @return the old types to update
     */
    public EkgRepository getOldRepository() {
        return viewModel.getOldRepository();
    }

    /**
     * Set the old EKG types.
     *
     * @param oldRepository the old types to update
     */
    public void setOldRepository(EkgRepository oldRepository) {
        viewModel.setOldRepository(oldRepository);
    }

    //================================================================================================================
    // internal logic to initialize and control the view
    //================================================================================================================


    /**
     * Initialize the view component behaviour like enable/disable switching
     * or or any other visible state changes (like specific prompt texts) that
     * based on the underlying view data.
     */
    private void initViewBehavior() {

        // register apply button to controller that can do anything with it.
        Node button = repositoryAdminDialogPane.lookupButton(ButtonType.APPLY);
        button.disableProperty().bind(
                viewModel.canOpenConnection().not().or(
                        viewModel.repositoryConnectionUrlIsValidProperty().not().or(
                                viewModel.repositoryNameIsValidProperty().not()
                        )
                ));

        // the types type combobox will disabled than an existing types will modified.
        // Change the types type of existing repository doesn't make sense.
        cbRepositoryDbType.disableProperty().bind(viewModel.oldRepositoryProperty().isNotNull());

        // enable/disable the username-password field depending on the Authentication type selection
        cbRepositoryAuthType.valueProperty().addListener((observable, oldValue, newAuthType) -> {
            txtRepositoryAuthUsername.setDisable(newAuthType != USERNAME_PASSWORD);
            txtRepositoryAuthPassword.setDisable(newAuthType == NONE);
        });

        // validation of url text field
        txtRepositoryUrl.textProperty().addListener((observable, oldValue, newUrl)  ->  {
            boolean urlIsValid = isConnectionUrlValid(newUrl);
            txtRepositoryUrl.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !urlIsValid && isNoneBlank(newUrl));
            viewModel.setRepositoryConnectionUrlIsValid(urlIsValid);
        });

        // validation of types name text field
        txtRepositoryName.textProperty().addListener((observable, oldValue, newName) -> {
            boolean nameIsValid = isValidRepositoryName(newName);
            txtRepositoryName.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !nameIsValid);
            viewModel.setRepositoryNameIsValid(nameIsValid);
        });

        // reload the list of known types names every time the dialog will display
        repositoryAdminDialogPane.layoutBoundsProperty().addListener(observable -> {
            knownRepositoryNames.clear();
            repository.getBoundedService(RepositoryService.class).listEkgRepositories()
                    .forEach(repository -> knownRepositoryNames.add(repository.getRepositoryName()));
        });

        // disable text field and according label for the Solr collection name if Solr Standalone is in use
        txtRepositoryIndexName.disableProperty().bind(viewModel.repositoryDbTypeProperty().isEqualTo(SOLR_STANDALONE));
        lbRepositoryIndexName.disableProperty().bind(txtRepositoryIndexName.disableProperty());

        cbRepositoryDbType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {

                case SOLR_CLOUD:
                    lbRepositoryUrl.setText("ZooKeeper Host/Port:");
                    txtRepositoryUrl.setPromptText("1-n ZooKeeper Host+Port Paare ('host:port, ...')");
                    break;

                case SOLR_STANDALONE:
                    lbRepositoryUrl.setText("Repository URL:");
                    txtRepositoryUrl.setPromptText("Absolute Solr URL incl. core name");
                    txtRepositoryIndexName.setText(null);
                    break;

                default:
                    lbRepositoryUrl.setText("Repository URL:");
                    txtRepositoryUrl.setPromptText("Insert repository URL");
            }
        });
    }

    /**
     * Initialize all handler that belongs to this component and will
     * proceed click actions or other event handling logic.
     */
    private void initHandler() {
        // no op
    }

    /**
     * Initialize the view styles that have to be configured programmatically.
     * (default styling should be define by style sheets)
     */
    private void initViewStyles() {
      cbRepositoryAuthType.setSkin(new EkgComboBoxListViewSkin<>(cbRepositoryAuthType, authTypeResourceMap, NONE));
      cbRepositoryDbType.setSkin(new EkgComboBoxListViewSkin<>(cbRepositoryDbType, dbTypeResourceMap, SOLR_STANDALONE));
    }

    /**
     * Initialize the view components with data by binding the model data to it
     * or set it directly.
     */
    private void initModelToView() {
        txtRepositoryName.textProperty().bindBidirectional(viewModel.repositoryNameProperty());

        cbRepositoryDbType.valueProperty().bindBidirectional(viewModel.repositoryDbTypeProperty());
        cbRepositoryDbType.setValue(SOLR_STANDALONE);
        cbRepositoryDbType.getItems().addAll(List.of(SOLR_STANDALONE, SOLR_CLOUD, ELASTICSEARCH_STANDALONE));

        txtRepositoryUrl.textProperty().bindBidirectional(viewModel.repositoryConnectionUrlProperty());
        txtRepositoryIndexName.textProperty().bindBidirectional(viewModel.dbIndexNameProperty());

        cbRepositoryAuthType.valueProperty().bindBidirectional(viewModel.repositoryAuthTypeProperty());
        cbRepositoryAuthType.setValue(NONE);
        cbRepositoryAuthType.getItems().addAll(ResourceAuthType.values());

        txtRepositoryAuthUsername.textProperty().bindBidirectional(viewModel.repositoryAuthUsernameProperty());
        txtRepositoryAuthPassword.textProperty().bindBidirectional(viewModel.repositoryAuthPasswordProperty());
    }


    //================================================================================================================
    // internal handlers that implement the controller business logic
    //================================================================================================================


    private boolean isValidRepositoryName(String newName) {

        if (viewModel.isEditingMode()) {
            return StringUtils.isNotBlank(newName)
                    && (newName.equals(viewModel.getOldRepository().getRepositoryName())
                    || !knownRepositoryNames.contains(newName));
        } else {
            return !knownRepositoryNames.contains(newName);
        }
    }


    private boolean isConnectionUrlValid(String newUrl) {

        if (viewModel.getRepositoryDbType() == SOLR_CLOUD) {
            return ZK_HOSTS_VALIDATION_PATTERN.matcher(newUrl).matches();
        }

        return REPOSITORY_URL_VALIDATOR.isValid(newUrl);
    }

    /**
     * Resets the view to initial state
     */
    private void resetViewStates() {
        txtRepositoryName.setText(null);
        txtRepositoryAuthUsername.setText(null);
        txtRepositoryAuthUsername.setText(null);
        cbRepositoryAuthType.setValue(NONE);
        cbRepositoryDbType.setValue(SOLR_STANDALONE);
        viewModel.oldRepositoryProperty().set(null);
        viewModel.setIsEditingMode(false);
        viewModel.setDbIndexName(null);
    }

    /**
     * Class to handle the open connection event. Sends a ExplorerUpdate-Event via the event-Bus.
     *
     * @param buttonType the type of the pressed button
     * @return the created types
     */
    protected EkgRepository openConnection(ButtonType buttonType) {

        // reset state if not clicked the apply button
        if (buttonType != ButtonType.APPLY) {
            resetViewStates();
            return null;
        }

        RepositoryService configService = repository.getBoundedService(RepositoryService.class);
        if (getOldRepository() != null) {
            configService.deleteRepository(getOldRepository());
        }

        String reproConnectionUrl = viewModel.getRepositoryConnectionUrl();

        if (reproConnectionUrl.endsWith("/") && viewModel.getRepositoryDbType() != ELASTICSEARCH_STANDALONE) {
            reproConnectionUrl = reproConnectionUrl.substring(0, reproConnectionUrl.length() - 1);
        }

        EkgRepository createdRepository = configService.addEkgRepository(
                viewModel.getRepositoryName(),
                reproConnectionUrl,
                viewModel.getDbIndexName(),
                viewModel.getRepositoryDbType(),
                viewModel.getRepositoryAuthType(),
                viewModel.getRepositoryAuthUsername(),
                viewModel.getRepositoryAuthPassword());

        eventBus.publish(new ExplorerUpdateEvent(this));

        return createdRepository;
    }
}