//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common;

import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.common.ui.view.AppWindowProvider;
import de.qaware.ekg.awb.commons.beans.BeanProvider;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.MetricsBookmarkService;
import de.qaware.ekg.awb.metricanalyzer.bl.bookmarks.et.BookmarkGroup;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.BookmarkGroupEvent;
import de.qaware.ekg.awb.metricanalyzer.ui.bookmarks.EditBookmarkGroupDialog;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ExplorerUpdateEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Optional;

import static de.qaware.ekg.awb.sdk.core.utils.TaskUtils.runAsTask;

/**
 * A tree node item that represents a domain specific/topic specific group
 * of bookmarks that are defined by a EKG user to group a specific set of bookmarks.
 */
@SuppressWarnings({"CdiManagedBeanInconsistencyInspection", "CdiInjectionPointsInspection"})
public class BookmarkGroupItem extends ContextAwareItem {

    private final BookmarkGroup bookmarkGroup;

    @Inject
    private Event<BookmarkGroupEvent> bookmarkEvent;

    @Inject
    private AppWindowProvider appWindowProvider;

    @Inject
    private EkgEventBus eventBus;

    /**
     * Creates a new BookmarkGroupItem based on the given queryParams using 'Bookmarks' as name
     *
     * @param bookmarkGroup the {@link BookmarkGroup} instance this explorer node represents (and is bind to)
     * @param queryParams - the query context.
     * @param repository   the containing types.
     */
    public BookmarkGroupItem(BookmarkGroup bookmarkGroup, FilterContext queryParams, EkgRepository repository) {
        super(bookmarkGroup.getName(), queryParams, repository);
        super.setGraphic(getIconProvider().getBookmarkGroupItemIcon(this));
        this.bookmarkGroup = bookmarkGroup;

        BeanProvider.injectFields(this);

        addContextMenuEntry("Delete Group",

                event -> {

                    Alert alert = new Alert(Alert.AlertType.WARNING);

                    DialogPane alertPane = alert.getDialogPane();
                    alertPane.getStylesheets().add("/de/qaware/ekg/awb/metricanalyzer/ui/bookmarks/CreateBookmarkDialogStyle.css");
                    Stage stage = (Stage) alertPane.getScene().getWindow();
                    stage.getIcons().add(new Image("/de/qaware/ekg/awb/metricanalyzer/ui/icons/warning.png"));
                    ImageView imageView = new ImageView("/de/qaware/ekg/awb/metricanalyzer/ui/icons/warning.png");
                    imageView.setFitWidth(36);
                    imageView.setFitHeight(33);
                    alertPane.setGraphic(imageView);

                    alert.setTitle("Confirming delete");
                    alert.setHeaderText("Bookmarks threatened!");
                    alert.setContentText("When deleting the group all assigned bookmarks will also be deleted irreversibly. " +
                            "This applies to all users of the repository.\n\n Do you really want to delete the group?\n ");
                    alert.getButtonTypes().add(ButtonType.YES);
                    alert.getButtonTypes().remove(ButtonType.OK);
                    alert.getButtonTypes().add(ButtonType.CANCEL);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.YES){
                        runAsTask(
                                "delete-bookmarkGroup-" + bookmarkGroup.getName() + "-task",
                                this::deleteBookmarkGroup,
                                finishedEvent -> eventBus.publish(new ExplorerUpdateEvent(getParent()))
                        );
                    }

                }
        );

        addContextMenuEntry("Rename group",
                event -> runAsTask(
                        "rename-bookmarkGroup-" + bookmarkGroup.getName() + "-task",
                        this::renameBookmarkGroup,
                        finishedEvent -> eventBus.publish(new ExplorerUpdateEvent(getParent()))
                )
        );
    }

    /**
     * Returns the unique id of the underlying bookmark group
     * that is represented by this tree item.
     *
     * @return the unique id of th bookmark group
     */
    public String getBookmarkGroupId() {
        return bookmarkGroup.getBookmarkGroupId();
    }

    @Override
    public EventHandler<KeyEvent> getEnterPressedHandler() {
        return e -> bookmarkEvent.fire(new BookmarkGroupEvent(this, bookmarkGroup, this.getRepository()));
    }


    private void renameBookmarkGroup() {

        EditBookmarkGroupDialog dialog = new EditBookmarkGroupDialog(bookmarkGroup, getRepository());
        dialog.initOwner(appWindowProvider.getAppWindow());

        dialog.showAndWait().ifPresent(bookmarkGroup -> {
            MetricsBookmarkService service = getRepository().getBoundedService(MetricsBookmarkService.class);
            service.initializeService(getRepository().getRepositoryClient());
            service.persistNewBookmarkGroup(bookmarkGroup);
            eventBus.publish(new ExplorerUpdateEvent(this, getRepository()));
        });

    }

    private void deleteBookmarkGroup() {
        getRepository().getBoundedService(MetricsBookmarkService.class).deleteBookmarkGroup(bookmarkGroup.getBookmarkGroupId());
        Platform.runLater(() -> ((AbstractItem<String>) getParent()).getService().restart());
    }
}