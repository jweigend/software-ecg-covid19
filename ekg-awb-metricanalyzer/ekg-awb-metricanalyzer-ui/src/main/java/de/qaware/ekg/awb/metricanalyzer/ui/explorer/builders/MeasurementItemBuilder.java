package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.MeasurementItem;

import java.util.List;

/**
 * TreeItem builder that produces TreeItem instances of type MeasurementItem
 */
public class MeasurementItemBuilder<T extends RepositoryBaseItem>  extends AbstractItemBuilder<T> {

    @Override
    protected List<MeasurementItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<Measurement> measurements = metricDataAccess.getMeasurements(new MetricQuery(context.getQueryParameter()));

        return ItemBuilder.getItems(measurements.stream(),
                measurement -> {
                    QueryFilterParams parentParams = context.getQueryParameter();
                    QueryFilterParams childQueryParams = new QueryFilterParams.Builder(parentParams).withMeasurement(measurement).build();
                    return new MeasurementItem(measurement, context.cloneWith(childQueryParams), item.getRepository());
                },
                notifier,
                measurements.size(),
                "Measurements"
        );
    }
}