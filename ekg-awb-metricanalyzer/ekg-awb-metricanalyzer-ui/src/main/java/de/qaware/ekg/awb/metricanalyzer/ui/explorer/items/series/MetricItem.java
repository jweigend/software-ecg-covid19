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
package de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series;

import de.qaware.ekg.awb.common.ui.events.OpeningMode;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryContextEvent;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.ContextAwareItem;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.sdk.core.events.EkgEventBus;
import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;
import de.qaware.ekg.awb.sdk.datamodel.Counter;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Explorer tree item for a single counter metric.
 */
public class MetricItem extends ContextAwareItem {

    private QueryFilterParams queryParams;

    private EkgEventBus eventBus = EkgLookup.lookup(EkgEventBus.class);

    /**
     * Creates a MetricItem with the given parameter.
     *
     * @param counter    the counter to use as name
     * @param filterContext bean that define filters that relating to series data or the view flavor and control the item rendering
     * @param queryParams an MetricQueryParams instance with all filter params need to get the data this item represents
     * @param repository the containing types.
     */
    public MetricItem(Counter counter, FilterContext filterContext, QueryFilterParams queryParams, EkgRepository repository) {
        super(counter.getCounterName(), filterContext, repository);
        super.setGraphic(getIconProvider().getMetricItemIcon(this));
        this.queryParams = queryParams;


        addContextMenuEntry("Show and keep existing metrics", e -> eventBus.publish(
                new QueryContextEvent(this, queryParams, getRepository(), OpeningMode.MERGE_VIEW)));

        addContextMenuEntry("Show and replace existing metrics", e -> eventBus.publish(
                new QueryContextEvent(this, queryParams, getRepository(), OpeningMode.CLEAR_VIEW)));

    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    /**
     * Gets the context of the current item
     *
     * @return the query context
     */
    public QueryFilterParams getQueryParams() {
        return queryParams;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventHandler<MouseEvent> getDoubleClickHandler() {
        return e -> eventBus.publish(new QueryContextEvent(this, new QueryFilterParams.Builder(getQueryParams())
                .withMultiMetricMode(false).build(), getRepository(), OpeningMode.CLEAR_VIEW));
    }

    /**
     * Get the event handler for a enter pressed event on the explorerView item.
     * <p>
     * The default handler do nothing.
     *
     * @return The event handler.
     */
    @Override
    @SuppressWarnings("unchecked")
    public EventHandler<KeyEvent> getEnterPressedHandler() {
        return e -> {
            //Avoid stack overflow for to many values
            if ("*".equals(getQueryParams().getMetricName())) {
                return;
            }

            eventBus.publish(
                    new QueryContextEvent(this, new QueryFilterParams.Builder(getQueryParams())
                            .withMultiMetricMode(true).build(), getRepository(), OpeningMode.CLEAR_VIEW));
        };
    }
}