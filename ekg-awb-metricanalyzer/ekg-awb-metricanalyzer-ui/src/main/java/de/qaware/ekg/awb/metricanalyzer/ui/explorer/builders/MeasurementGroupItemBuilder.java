package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.MeasurementGroupItem;
import de.qaware.ekg.awb.sdk.awbapi.project.ProjectViewFlavor;

import java.util.List;

/**
 * TreeItem builder that produces Measurements TreeItem instances.
 */
public class MeasurementGroupItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {


    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return parentFilterContext.getQueryParameter().getMeasurement() == Measurement.DEFAULT &&
                  parentFilterContext.getActiveViewFlavor() == ProjectViewFlavor.PHYSICAL_VIEW;
    }

    @Override
    protected List<MeasurementGroupItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {
        return List.of(new MeasurementGroupItem(context, item.getRepository()));
    }
}
