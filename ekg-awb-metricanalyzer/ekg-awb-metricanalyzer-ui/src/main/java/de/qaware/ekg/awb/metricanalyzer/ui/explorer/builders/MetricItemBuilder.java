package de.qaware.ekg.awb.metricanalyzer.ui.explorer.builders;

import de.qaware.ekg.awb.common.ui.explorer.api.ProgressNotifier;
import de.qaware.ekg.awb.explorer.ui.items.ItemBuilder;
import de.qaware.ekg.awb.explorer.ui.items.common.RepositoryBaseItem;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.metricanalyzer.ui.api.FilterContext;
import de.qaware.ekg.awb.metricanalyzer.ui.explorer.items.series.MetricItem;
import de.qaware.ekg.awb.sdk.awbapi.repository.ImporterSourceRepository;
import de.qaware.ekg.awb.sdk.awbapi.repository.SourceRepositoryService;
import de.qaware.ekg.awb.sdk.core.log.EkgLogger;
import de.qaware.ekg.awb.sdk.core.lookup.ServiceDiscovery;
import de.qaware.ekg.awb.sdk.datamodel.Counter;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * TreeItem builder that produces TreeItem instances of type MetricItem
 */
public class MetricItemBuilder<T extends RepositoryBaseItem> extends AbstractConditionalItemBuilder<T> {

    private static final Logger LOGGER = EkgLogger.get();

    @Override
    protected boolean isValidParentForChilds(T parentItem, FilterContext parentFilterContext) {
        return true;
    }

    @Override
    protected List<MetricItem> getChildItemsFor(T item, FilterContext context, ProgressNotifier notifier) {

        MetricDataAccessService metricDataAccess = item.getRepository().getBoundedService(MetricDataAccessService.class);

        List<Metric> metrics = metricDataAccess.getMetricsNames(new MetricQuery(context.getQueryParameter()));

        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return ItemBuilder.getItems(
                    metrics.parallelStream(),
                    new MetricMapper(context, item),
                    notifier, metrics.size(),
                    "Metrics");
        } finally {
            LOGGER.info("** Metrics loaded in: " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms");
        }

    }



    private static class MetricMapper implements Function<Metric, MetricItem> {

        private FilterContext parentFilterContext;

        private QueryFilterParams queryParams;

        private RepositoryBaseItem parentItem;

        private ImporterSourceRepository importerSourceRepository;

        public MetricMapper(FilterContext filterContext, RepositoryBaseItem parentItem) {
            this.parentFilterContext = filterContext;
            this.parentItem = parentItem;
            this.queryParams = filterContext.getQueryParameter();

            SourceRepositoryService service = ServiceDiscovery.lookup(SourceRepositoryService.class, parentItem.getRepository());
            importerSourceRepository = service.queryImportSourceRepository(queryParams.getProjectName());
        }

        @Override
        public MetricItem apply(Metric metric) {

            QueryFilterParams query = new QueryFilterParams.Builder<>(queryParams)
                    .withMetric(metric)
                    .withImporterSourceRepository(importerSourceRepository)
                    .build();

            return new MetricItem(
                    new Counter(metric.getName()),
                    parentFilterContext.cloneWith(query),
                    query,
                    parentItem.getRepository()
            );
        }
    }
}