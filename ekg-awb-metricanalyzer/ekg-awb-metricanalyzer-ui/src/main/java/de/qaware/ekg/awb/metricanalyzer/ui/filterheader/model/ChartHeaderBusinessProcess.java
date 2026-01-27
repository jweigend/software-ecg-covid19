//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.model;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.MetricDataAccessService;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Metric;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.MetricGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Container;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Namespace;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Pod;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.Host;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.physical.HostGroup;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.MetricQuery;
import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.query.QueryFilterParams;
import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.dataobject.facet.FacetParams;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business Processes for the chart filter header
 */
public class ChartHeaderBusinessProcess extends ChartHeaderModel {

    // classic filters
    private Service<Collection<HostGroup>> hostGroupsService;
    private Service<Collection<Host>> hostsService;

    // cloud native filters
    private Service<Collection<Namespace>> namespacesService;
    private Service<Collection<de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service>> servicesService;
    private Service<Collection<Pod>> podsService;
    private Service<Collection<Container>> containersService;

    // generic filters
    private Service<Collection<Measurement>> measurementsService;
    private Service<Collection<Process>> procService;
    private Service<Collection<MetricGroup>> metricGroupsService;
    private Service<Collection<Metric>> metricNamesService;


    public void setRepository(EkgRepository repository) {

        if (getRepository() == repository) {
            return;
        }

        super.setRepository(repository);

        MetricDataAccessService metricDAS = getRepository().getBoundedService(MetricDataAccessService.class);

        hostGroupsService = createService(super.getHostGroups(), b -> b.withHostGroup((HostGroup) null), metricDAS::getHostGroups);
        hostsService = createService(super.getHosts(), b -> b.withHost((Host) null), metricDAS::getHosts);

        namespacesService = createService(super.getNamespaces(), b -> b.withNamespace((Namespace) null), metricDAS::getNamespaces);
        servicesService = createService(super.getServices(),
                b -> b.withService((de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.logical.Service) null), metricDAS::getServices);
        podsService = createService(super.getPods(), b -> b.withPod((Pod) null), metricDAS::getPods);
        containersService = createService(super.getContainers(), b -> b.withContainer((Container) null), metricDAS::getContainers);

        measurementsService = createService(super.getMeasurements(), b -> b.withMeasurement((Measurement) null), metricDAS::getMeasurements);
        procService = createService(super.getProcess(), b -> b.withProcess((Process) null), metricDAS::getProcesses);
        metricGroupsService = createService(super.getMetricMetricGroups(), b -> b.withMetricGroup((MetricGroup) null), metricDAS::getMetricGroups);
        metricNamesService = createService(super.getMetrics(), b -> b.withMetric((Metric) null), metricDAS::getMetricsNames);

    }

    private <T> Service<Collection<T>> createService(ObservableList<T> itemsList,
                                              final Function<QueryFilterParams.Builder, QueryFilterParams.Builder<?>> builder,
                                              final Function<MetricQuery, Collection<T>> fetcher) {

        return new Service<>() {
            @Override
            protected Task<Collection<T>> createTask() {
                return new Task<>() {
                    @Override
                    protected Collection<T> call() {
                        QueryFilterParams.Builder<QueryFilterParams> filterBuilder = new QueryFilterParams.Builder<>(
                                ChartHeaderBusinessProcess.super.asQueryParams()
                        ).withSort(FacetParams.FacetSort.LEXICOGRAPHIC);

                        return fetcher.apply(new MetricQuery(filterBuilder.build()));
                    }

                    @Override
                    protected void succeeded() {
                        try {
                            Collection<T> result = super.get();
                            if (result != null) {
                                itemsList.setAll(result);
                            } else {
                                itemsList.clear();
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(ChartHeaderBusinessProcess.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
            }
        };
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadProcesses() {
        procService.restart();
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadHostGroups() {
        hostGroupsService.restart();
    }

    /**
     * Loads the hosts and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadHosts() {
        hostsService.restart();
    }

    /**
     * Loads the groups and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadMetricGroups() {
        metricGroupsService.restart();
    }

    /**
     * Loads the measurements and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadMeasurements() {
        measurementsService.restart();
    }

    /**
     * Loads the metrics and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadMetrics() {
        metricNamesService.restart();
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadNamespaces() {
        namespacesService.restart();
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadServices() {
        servicesService.restart();
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadPods() {
        podsService.restart();
    }

    /**
     * Loads the processes and fills the model.
     * The model indicates a change to the bound control
     */
    public void loadContainers() {
        containersService.restart();
    }
}
