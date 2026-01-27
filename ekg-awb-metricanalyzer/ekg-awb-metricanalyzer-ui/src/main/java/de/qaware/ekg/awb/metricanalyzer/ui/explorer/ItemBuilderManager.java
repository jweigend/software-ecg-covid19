package de.qaware.ekg.awb.metricanalyzer.ui.explorer;

import de.qaware.ekg.awb.common.ui.explorer.api.ItemBuilderRegistry;
import de.qaware.ekg.awb.explorer.ui.items.common.ProjectItem;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders.*;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.BookmarkGroupItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.common.ReproBookmarksItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.*;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.logical.*;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostGroupItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostItem;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.physical.HostsRootItem;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;

/**
 * Manager that register suitable combinations of TreeItemBuilder for the
 * specific parent TreeItem types.
 */
public class ItemBuilderManager {

    public static void registerTreeItemBuilders() {

        // retrieve central builder registry
        ItemBuilderRegistry builderRegistry = EkgLookup.lookup(ItemBuilderRegistry.class);
        registerHostGroupItemBuilder(builderRegistry);
        registerNamespaceGroupItemBuilder(builderRegistry);
        registerProcessItemBuilder(builderRegistry);
        registerMeasurementsItemBuilder(builderRegistry);
        registerCounterItemBuilder(builderRegistry);
        registerServiceGroupItemBuilder(builderRegistry);
        registerPodGroupItemBuilder(builderRegistry);
        registerBookmarkItemBuilder(builderRegistry);

        // various others
        builderRegistry.registerItemBuilder(MetricGroupItem.class, new MetricItemBuilder<>());
        builderRegistry.registerItemBuilder(PodItem.class, new MetricGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ServiceItem.class, new MetricGroupItemBuilder<>());
    }

    /**
     * Register all item builder's that are required to create the bookmark group / bookmark items structure
     * for the current EKG repository.
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private static void registerBookmarkItemBuilder(ItemBuilderRegistry builderRegistry) {
        builderRegistry.registerItemBuilder(RepositoryItem.class, new ReproBookmarksItemBuilder<>());
        builderRegistry.registerItemBuilder(ReproBookmarksItem.class, new BookmarkGroupItemBuilder<>());
        // list items that are in global space
        builderRegistry.registerItemBuilder(ReproBookmarksItem.class, new BookmarkItemBuilder<>());
        // list items that are assigned to a specific group
        builderRegistry.registerItemBuilder(BookmarkGroupItem.class, new BookmarkItemBuilder<>());
    }

    /**
     * Register an item builder for HostItem instances for different parent
     * items at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private static void registerHostGroupItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(ProjectItem.class, new HostsRootItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementItem.class, new HostsRootItemBuilder<>());

        builderRegistry.registerItemBuilder(HostsRootItem.class, new HostGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(HostsRootItem.class, new HostItemBuilder<>());

        builderRegistry.registerItemBuilder(HostGroupItem.class, new HostItemBuilder<>());
    }

    private static void registerNamespaceGroupItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(ProjectItem.class, new NamespaceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessItem.class, new NamespaceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementItem.class, new NamespaceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(NamespaceGroupItem.class, new NamespaceItemBuilder<>());
    }

    private static void registerServiceGroupItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(ProjectItem.class, new ServiceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessItem.class, new ServiceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementItem.class, new ServiceGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ServiceGroupItem.class, new ServiceItemBuilder<>());
    }

    private static void registerPodGroupItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(ProjectItem.class, new PodGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessItem.class, new PodGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ServiceItem.class, new PodGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementItem.class, new PodGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(NamespaceItem.class, new PodGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(PodGroupItem.class, new PodItemBuilder<>());
    }

    /**
     * Register an item builder for ProcessItem instances for different parent
     * items at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private static void registerProcessItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(ProjectItem.class, new ProcessGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(PodItem.class, new ProcessGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(HostItem.class, new ProcessGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessGroupItem.class, new ProcessItemBuilder<>());
    }

    /**
     * Register an item builder for MeasurementGroupItem instances for different parent
     * items at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private static void registerMeasurementsItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(HostItem.class, new MeasurementGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessItem.class, new MeasurementGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(ProjectItem.class, new MeasurementGroupItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementGroupItem.class, new MeasurementItemBuilder<>());
    }

    /**
     * Register an item builder for MeasurementGroupItem instances for different parent
     * items at the given ItemBuilderRegistry
     *
     * @param builderRegistry the builder registry that should used to register the builders
     */
    private static void registerCounterItemBuilder(ItemBuilderRegistry builderRegistry) {

        // register it to different parents
        builderRegistry.registerItemBuilder(HostItem.class, new CounterItemBuilder<>());
        builderRegistry.registerItemBuilder(ProcessItem.class, new CounterItemBuilder<>());
        builderRegistry.registerItemBuilder(MeasurementItem.class, new CounterItemBuilder<>());
        builderRegistry.registerItemBuilder(ProjectItem.class, new CounterItemBuilder<>());
        builderRegistry.registerItemBuilder(CounterItem.class, new MetricGroupItemBuilder<>());
    }
}
