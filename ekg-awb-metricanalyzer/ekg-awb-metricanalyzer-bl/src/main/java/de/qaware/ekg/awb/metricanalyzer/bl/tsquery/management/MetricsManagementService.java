//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.tsquery.management;


import de.qaware.ekg.awb.sdk.awbapi.repository.RepositoryException;
import de.qaware.ekg.awb.sdk.datamodel.TimeSeries;

import java.util.stream.Stream;

/**
 * The MetricsManagementService performs the import of metrics into a types.
 */
public interface MetricsManagementService {

    /**
     * Imports the given {@link TimeSeries} into the backing data storage.
     * <p>
     * If the series already exists the values will be added to the existing series.
     *
     * @param timeSeries             The time series for import.
     * @param overrideExistingSeries if override == true the existing series will be deleted.
     */
    void importTimeSeries(Stream<TimeSeries> timeSeries, boolean overrideExistingSeries) throws RepositoryException;

    /**
     * Commits the data that were written to the repository
     * before.
     */
    void commit();
}
