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
package de.qaware.ekg.awb.commons.about;

import java.time.Instant;

/**
 * Interface to get the applications version info.
 */
public interface VersionInfo {
    /**
     * Get the scm revision (GIT commitID, SVN Revision) this application was built from.
     *
     * @return The scm revision.
     */
    String getBuildRevision();

    /**
     * Get the date & time when this application was built.
     *
     * @return The build date & time.
     */
    Instant getBuildTime();

    /**
     * Get the version string for this application.
     *
     * @return The version string.
     */
    String getVersionString();
}
