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

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link URIStringConverter}.
 */
public class URIStringConverterTest {

    public static final String TEST_URI_STR = "http://localhost";
    public static final URI TEST_URI = URI.create(TEST_URI_STR);
    private URIStringConverter converter = new URIStringConverter();

    @Test
    public void testToString() throws Exception {
        assertThat(converter.toString(null), is(equalTo("")));
        assertThat(converter.toString(URI.create("")), is(equalTo("")));
        assertThat(converter.toString(TEST_URI), is(equalTo("http://localhost")));
    }

    @Test
    public void testFromString() throws Exception {
        assertThat(converter.fromString(null), is(equalTo(URI.create(""))));
        assertThat(converter.fromString(""), is(equalTo(URI.create(""))));
        assertThat(converter.fromString("asdf asdf"), is(nullValue()));
        assertThat(converter.fromString(TEST_URI_STR), is(equalTo(TEST_URI)));
    }
}
