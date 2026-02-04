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
package de.qaware.ekg.awb.metricanalyzer.ui.filterheader.converter;

import de.qaware.ekg.awb.metricanalyzer.bl.tsquery.et.Measurement;
import javafx.util.StringConverter;

public class MeasurementStringConverter extends StringConverter<Measurement> {

    @Override
    public String toString(Measurement measurement) {
        if (measurement != null) {
            return measurement.getName();
        }
        return "*";
    }

    @Override
    public Measurement fromString(String s) {
        if (s != null) {
            return new Measurement(s, -1, -1);
        }
        return new Measurement("*", -1, -1);
    }
}
