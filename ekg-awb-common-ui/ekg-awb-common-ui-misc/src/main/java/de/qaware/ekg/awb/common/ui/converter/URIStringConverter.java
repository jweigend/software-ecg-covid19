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
package de.qaware.ekg.awb.common.ui.converter;

import javafx.util.StringConverter;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * URI String Converter.
 */
public class URIStringConverter extends StringConverter<URI> {
    @Override
    public String toString(URI object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    @Override
    public URI fromString(String string) {
        try {

            return new URI(defaultIfNull(string, "").replace('\\', '/'));
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
