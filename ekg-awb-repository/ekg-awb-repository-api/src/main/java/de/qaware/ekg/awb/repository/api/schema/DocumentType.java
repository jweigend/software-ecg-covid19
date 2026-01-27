//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//       created by:    Johannes Weigend
//       creation date: 8.5.2012
//       changed by:    $Author$
//       change date:   $Date$
//       revision:      $Revision$
//       description:   Type field for solr table scheme.
//______________________________________________________________________________
//
//        Copyright:    QAware GmbH
//______________________________________________________________________________
package de.qaware.ekg.awb.repository.api.schema;


/**
 * An enumeration that represents the different types of
 * document types in the EKG repository/storage.
 * The DocType has the similar meaning and functionality as
 * the Discriminator known from traditional databases.
 */
public enum DocumentType {

    /**
     * time series data with a metadata block and
     * (in case of local project type) time-value tuples
     * that represent the series
     */
    TIME_SERIES,

    /**
     * Default bookmark data that will store the setup
     * of the displayed metric panel with all background
     * charts and aggregation/color settings
     */
    METRIC_BOOKMARK,

    /**
     * A group that groups all bookmarks that belongs to
     * a specific domain or topic. This can be a concrete analysis,
     * project or something else.
     */
    BOOKMARK_GROUP,

    /**
     * A repository definition that contains the connection
     * data and some meta data of (remote) repositories
     * stored as config in the current storage.
     */
    REPOSITORY,

    /**
     * An EKG AWB project that gives a set of time series the
     * necessary context to display interpret them correctly.
     */
    PROJECT,

    /**
     * All type of documents
     * (= equal behaviour to no doc type filter)
     */
    ALL
}
