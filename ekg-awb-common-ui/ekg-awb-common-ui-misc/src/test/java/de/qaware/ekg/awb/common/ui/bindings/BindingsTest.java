//______________________________________________________________________________
//
//                  Project:    Software EKG
//______________________________________________________________________________
//
//                   Author:    QAware GmbH 2009 - 2021
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.common.ui.bindings;

import de.qaware.ekg.awb.common.ui.converter.Converter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link Bindings}.
 */
public class BindingsTest {

    private IntegerProperty property1 = new SimpleIntegerProperty();
    private StringProperty property2 = new SimpleStringProperty("0");

    @Test
    public void testBindBidirectional() throws Exception {
        Bindings.bindBidirectional(property1, property2, Object::toString, Integer::parseInt);

        property1.setValue(5);
        assertThat(property2.getValue(), is(equalTo("5")));
        property2.setValue("10");
        assertThat(property1.getValue(), is(equalTo(10)));
    }

    @Test
    public void testBindAndUnbindBidirectional() throws Exception {
        Bindings.bindBidirectional(property1, property2, new Converter<Number, String>() {
            @Override
            public String fromFirst(Number first) {
                return first.toString();
            }

            @Override
            public Number fromSecond(String second) {
                return Integer.parseInt(second);
            }
        });

        property1.setValue(5);
        assertThat(property2.getValue(), is(equalTo("5")));
        property2.setValue("10");
        assertThat(property1.getValue(), is(equalTo(10)));

        Bindings.unbind(property1, property2);
        property1.setValue(6);
        assertThat(property2.getValue(), is(equalTo("10")));
        property2.setValue("7");
        assertThat(property1.getValue(), is(equalTo(6)));
    }

    @Test
    public void testBindBidirectional1() throws Exception {
        IntegerProperty property2 = new SimpleIntegerProperty(4);
        IntegerProperty property3 = new SimpleIntegerProperty(4);
        Bindings.bindBidirectional(property1, property2, property3);

        property1.setValue(5);
        assertThat(property2.getValue(), is(equalTo(4)));
        assertThat(property3.getValue(), is(equalTo(5)));

        property2.setValue(6);
        assertThat(property1.getValue(), is(equalTo(6)));
        assertThat(property3.getValue(), is(equalTo(5)));
    }
}
