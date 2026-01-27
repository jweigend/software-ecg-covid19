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
package de.qaware.ekg.awb.common.ui.explorer.impl;

import de.qaware.ekg.awb.common.ui.explorer.api.AbstractItem;
import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.sdk.core.events.AwbErrorEvent;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.events.ProgressEvent;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A task to build child items for a given parent item.
 *
 * @param <T> The type of the parent item.
 */
class ChildrenTask<T extends AbstractItem> extends Task<List<AbstractItem>> implements ProgressNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChildrenTask.class);
    private final T parentItem;
    private final ItemBuilderRegistryImpl registry;
    private final EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    /**
     * @param parentItem the parent item.
     * @param registry   The registry instance created this task.
     */
    public ChildrenTask(T parentItem, ItemBuilderRegistryImpl registry) {
        this.parentItem = parentItem;
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<AbstractItem> call() {
        try {
            return registry.getHandlerFor(parentItem.getClass())
                    .stream()
                    .flatMap(f -> ((BiFunction<T, ProgressNotifier, List<? extends AbstractItem>>) f).apply(parentItem, this).stream())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            EkgEventBus bus = EkgLookup.lookup(EkgEventBus.class);
            bus.publish(new AwbErrorEvent(parentItem, e));
            throw e;
        }
    }

    @Override
    protected void failed() {
        super.failed();
        updateProgress("Can not load child items of explorerView. Cause: " + getException().getMessage(), 0, 0);
        LOGGER.error("Can not load child items of explorerView", getException());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateMessage(String message) {
        super.updateMessage(message);
        eventBus.publish(new ProgressEvent(message, 0, this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
        double progress = workDone / max;
        if (!Objects.equals(progress, 1.0)) {
            eventBus.publish(new ProgressEvent("Loading sub entries", progress, this));
        } else {
            eventBus.publish(new ProgressEvent("", progress, this));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateProgress(String message, double workDone, double max) {
        super.updateProgress(workDone, max);
        super.updateMessage(message);
        eventBus.publish(new ProgressEvent(message, workDone / max, this));
    }
}
