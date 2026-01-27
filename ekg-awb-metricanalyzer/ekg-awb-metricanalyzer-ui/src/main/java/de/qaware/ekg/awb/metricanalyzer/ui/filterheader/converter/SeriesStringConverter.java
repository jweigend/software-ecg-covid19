//______________________________________________________________________________
//
//          ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//         Author:      QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.project.api.model.Project;
import javafx.util.StringConverter;

public class SeriesStringConverter extends StringConverter<Project> {

    @Override
    public String toString(Project project) {
        if (project != null) {
            return project.getName();
        }
        return "*";
    }

    @Override
    public Project fromString(String s) {
        if (s != null) {
            return new Project(s);
        }
        return new Project("*");
    }
}
