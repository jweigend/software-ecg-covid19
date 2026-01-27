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

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Process;
import javafx.util.StringConverter;

public class ProcessStringConverter extends StringConverter<Process> {

    @Override
    public String toString(Process proc) {
        if (proc != null) {
            return proc.getName();
        }
        return "*";
    }

    @Override
    public Process fromString(String s) {
        if (s != null) {
            return new Process(s, s);
        }
        return new Process("*", "*");
    }
}
