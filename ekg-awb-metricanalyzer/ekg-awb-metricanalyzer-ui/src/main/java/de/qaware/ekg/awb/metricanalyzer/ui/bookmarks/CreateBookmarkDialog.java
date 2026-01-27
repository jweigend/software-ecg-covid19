//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.common.ui.components.FilterableComboBox;
import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.Bookmark;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;


/**
 * A popup dialog that contains just a simple text field
 * to specify the name of a bookmark and persist the current
 * chart state into it.
 *
 * @param <T> the concrete bookmark type manged by this dialog instance
 */
@SuppressWarnings("unused") // assigned via JavaFX mechanism
public class CreateBookmarkDialog<T extends Bookmark> extends Dialog<BookmarkCreationResult<T>> {

    private static final BookmarkGroup NO_GROUP_DEFAULT = new BookmarkGroup("-no group-", BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID);

    private final Bookmark.Builder<T> bookmarkBuilder;

    private EkgRepository targetRepository;

    @FXML
    private TextField bookmarkName;

    @FXML
    private FilterableComboBox<BookmarkGroup> fcbBookmarkGroup;

    /**
     * Initialize a new create bookmark dialog.
     *
     * @param bookmarkBuilder The builder instance to create the concrete bookmark instance.
     * @param repository the repository used to store the bookmark
     */
    public CreateBookmarkDialog(Bookmark.Builder<T> bookmarkBuilder, EkgRepository repository) {

        this.bookmarkBuilder = bookmarkBuilder;

        try {
            BeanProvider.injectFields(this);

            FXMLLoader fxmlLoader = EkgLookup.lookup(FXMLLoader.class);
            fxmlLoader.setLocation(CreateBookmarkDialog.class.getResource("CreateBookmarkDialogView.fxml"));
            fxmlLoader.setController(this);

            setDialogPane(fxmlLoader.load());
            setTitle("Create bookmark");

            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("bookmark-icon.png").toExternalForm()));
            setResultConverter(this::createBookmark);

            Button applyButton = (Button)getDialogPane().lookupButton(ButtonType.APPLY);
            applyButton.getStyleClass().add("applyButton");
            applyButton.setDefaultButton(true);
            applyButton.setDisable(true);
            applyButton.setOnKeyReleased(event -> {
                if (!isValid()) {
                    event.consume();
                }
                if(event.getCode() == KeyCode.ENTER) {
                    handleKeyDownEnter();
                }
            });

            applyButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!isValid()) {
                    event.consume();
                }
                handleKeyDownEnter();
            });

            List<BookmarkGroup> bookmarkGroups = repository.getBoundedService(MetricsBookmarkService.class).getBookmarkGroups();
            bookmarkGroups.add(0, NO_GROUP_DEFAULT);
            fcbBookmarkGroup.getItems().setAll(FXCollections.observableArrayList(bookmarkGroups));
            fcbBookmarkGroup.getEditor().setOnMouseClicked(event -> {
                if (fcbBookmarkGroup.getValue() == null ||
                        BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID.equals(fcbBookmarkGroup.getValue().getBookmarkGroupId())) {
                    fcbBookmarkGroup.setValue(null);
                    fcbBookmarkGroup.getEditor().setText("");
                }
            });

            bookmarkName.textProperty().addListener(observable -> applyButton.setDisable(!isValid()));

            bookmarkName.requestFocus();
            targetRepository = repository;
        } catch (IOException e) {
            throw new IllegalStateException("Can not load fxml file for CreateBookmarkDialog", e);
        }
    }

    private void handleKeyDownEnter() {
        // do this to submit changes at combobox
        fcbBookmarkGroup.commitValue();
        // save results
        CreateBookmarkDialog.this.setResult(createBookmark(ButtonType.APPLY));

    }

    /**
     * Get the name of the bookmark.
     *
     * @return The bookmark name.
     */
    public String getName() {
        return bookmarkName.getText();
    }

    /**
     * Get the target types.
     *
     * @return the target types.
     */
    public EkgRepository getTargetRepository() {
        return targetRepository;
    }

    /**
     * Check if the current dialog state is valid.
     *
     * @return true if the dialog is valid. false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValid() {
        return StringUtils.isNotBlank(bookmarkName.getText()) && targetRepository != null;
    }

    private BookmarkCreationResult<T> createBookmark(ButtonType buttonType) {

        if (buttonType != ButtonType.APPLY || getTargetRepository() == null || StringUtils.isBlank(bookmarkName.getText())) {
            return null;
        }

        T bookmark = bookmarkBuilder.withName(bookmarkName.getText()).build();

        BookmarkCreationResult<T> result = new BookmarkCreationResult<>();

        // case 1: user didn't specify an bookmark group
        if (fcbBookmarkGroup.getValue() == null || NO_GROUP_DEFAULT.equals(fcbBookmarkGroup.getValue())) {
            BookmarkGroup group = new BookmarkGroup();
            group.setName("");
            group.setBookmarkGroupId(BookmarkGroup.DEFAULT_GLOBAL_GROUP_ID);
            result.setBookmarkGroup(group);
            bookmark.setBookmarkGroupId(group.getBookmarkGroupId());
        }
        // case 2: user defined a new bookmark group
        else if (fcbBookmarkGroup.getValue().isNew()) {
            BookmarkGroup group = fcbBookmarkGroup.getValue();
            result.setBookmarkGroup(group);
            bookmark.setBookmarkGroupId(null);
        }
        // case 3: user choose an existing bookmark group
        else {
            BookmarkGroup group = fcbBookmarkGroup.getValue();
            result.setBookmarkGroup(group);
            bookmark.setBookmarkGroupId(group.getBookmarkGroupId());
        }

        result.setBookmark(bookmark);
        return result;
    }

}
