package de.qaware.ekg.awb.metricanalyzer.ui.api;

import de.qaware.ekg.awb.explorer.ui.ExplorerTreeIconsProvider;
import javafx.scene.Node;


/**
 * A specialized variant of the {@link ExplorerTreeIconsProvider} class that
 * supports a lot more icon used for metric series specific explorer items.
 */
public interface MetricTreeIconsProvider extends ExplorerTreeIconsProvider {

    /**
     *
     *
     *
     * @param item
     * @return
     */
    Node getReproBookmarksItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents single (metric) bookmark.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getBookmarkItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of bookmarks.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getBookmarkGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a single metric (KPI).
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getMetricItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of metrics of the same domain.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getMetricGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a wrapper element
     * that contains multiple metric groups.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getCounterItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a measurement.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getMeasurementItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of measurements.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getMeasurementGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of host (=cluster).
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getHostGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a single host / physical node.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getHostItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of processes.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getProcessGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a single process.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getProcessItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a (Docker) container.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getContainerItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a wrapper element that
     * contains multiple groups of Cloud Platform namespaces (e.g. OpenShift projects).
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getNamespaceGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a single CloudPlatform namespace.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getNamespaceItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a Pod that runs in Kupernetes/OpenShift.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getPodItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a group of services as type
     * of load balancer for multiple pods in a CloudPlatform.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getServiceGroupItemIcon(ContextAwareItem item);

    /**
     * Returns the tree icon for a tree item that represents a single services as type
     * of load balancer for multiple pods in a CloudPlatform.
     * The returned icon is (svg or bitmap based) wrapped in a Node object.
     *
     * @param item the tree icon the provider should resolve the icon for
     * @return the icon wrapped into a (prototype) Node instance
     */
    Node getServiceItemIcon(ContextAwareItem item);

    /**
     * The default implementation of the MetricTreeIconsProvider interface that
     * can used to derive from it or if no other implementation exists.
     */
    MetricTreeIconsProvider DEFAULT = new DefaultTreeIconsProvider();
}
