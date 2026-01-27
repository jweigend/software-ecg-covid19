package de.qaware.ekg.awb.explorer.ui.items;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import javafx.scene.control.TreeItem;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for all ItemBuilder implementations that provides the
 * getItem() method used to map Item elements from a stream to TreeItems
 * including progress calculation.
 */
public class ItemBuilder {

    /**
     * Get the item objects for all objects within the given elements stream and send the required progress notification
     * messages. The item objects are created by the given {@code mapper} function.
     *
     * @param elements    The elements stream to map into items.
     * @param mapper      The mapper function to create an item from a given element.
     * @param notifier    The notifier object send the progress updates over.
     * @param numElements The whole number of elements to map. Needed to send the correct progress updates.
     * @param desc        A short description about what elements are loaded.
     * @param <T>         The type of the input elements.
     * @param <V>         The actual type of the items.
     * @return A list with the mapped items.
     */
    public static <T, V extends RepositoryBaseItem> List<V> getItems(Stream<T> elements,
                                                                     Function<T, V> mapper,
                                                                     ProgressNotifier notifier,
                                                                     int numElements, String desc) {
        String message = "Loading " + desc;
        notifier.updateProgress(message, -1, 100);
        AtomicInteger i = new AtomicInteger(1);
        List<V> items = elements
                .peek(h -> notifier.updateProgress(message, i.incrementAndGet(), numElements))
                .distinct()
                .map(mapper)
                .sorted(Comparator.comparing(TreeItem::getValue))
                .collect(Collectors.toList());

        notifier.updateProgress(1, 1);
        return items;
    }
}
