package de.qaware.ekg.awb.metricanalyzer.ui.bookmarks;

import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
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

/**
 * A dialog that will used to rename {@link BookmarkGroup} instance.
 */
public class EditBookmarkGroupDialog extends Dialog<BookmarkGroup> {

    private EkgRepository targetRepository;

    private BookmarkGroup bookmarkGroup;

    @FXML
    @SuppressWarnings("unused") // assigned via JavaFX mechanism
    private TextField bookmarkGroupName;

    /**
     * Initialize a new create bookmark dialog.
     *
     * @param bookmarkGroup a {@link BookmarkGroup} instance that should renamed
     * @param repository the repository used to store the bookmark
     */
    public EditBookmarkGroupDialog(BookmarkGroup bookmarkGroup, EkgRepository repository) {

        this.bookmarkGroup = bookmarkGroup;

        try {
            BeanProvider.injectFields(this);

            FXMLLoader fxmlLoader = EkgLookup.lookup(FXMLLoader.class);
            fxmlLoader.setLocation(CreateBookmarkDialog.class.getResource("EditBookmarkGroupDialogView.fxml"));
            fxmlLoader.setController(this);

            setDialogPane(fxmlLoader.load());
            setTitle("Edit bookmark group");

            Stage stage = (Stage) getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("bookmark-icon.png").toExternalForm()));
            setResultConverter(this::createBookmarkGroup);

            Button applyButton = (Button)getDialogPane().lookupButton(ButtonType.APPLY);
            applyButton.getStyleClass().add("applyButton");
            applyButton.setDefaultButton(true);
            applyButton.setDisable(true);
            applyButton.setOnKeyReleased(event -> {
                if (!isValid()) {
                    event.consume();
                }

                if(event.getCode() == KeyCode.ENTER) {
                    EditBookmarkGroupDialog.this.setResult(createBookmarkGroup(ButtonType.APPLY));
                }
            });

            applyButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!isValid()) {
                    event.consume();
                }
            });

            bookmarkGroupName.textProperty().addListener(observable -> applyButton.setDisable(!isValid()));

            bookmarkGroupName.requestFocus();
            targetRepository = repository;
        } catch (IOException e) {
            throw new IllegalStateException("Can not load fxml file for EditBookmarkGroupDialog", e);
        }
    }

    /**
     * Returns the name of the bookmark group.
     *
     * @return the name of the bookmark group
     */
    public String getName() {
        return bookmarkGroupName.getText();
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
        return StringUtils.isNotBlank(bookmarkGroupName.getText()) && targetRepository != null;
    }

    private BookmarkGroup createBookmarkGroup(ButtonType buttonType) {
        if (buttonType != ButtonType.APPLY || getTargetRepository() == null || StringUtils.isBlank(bookmarkGroupName.getText())) {
            return null;
        }

        BookmarkGroup group = new BookmarkGroup();
        group.setName(bookmarkGroupName.getText());
        return group;
    }
}
