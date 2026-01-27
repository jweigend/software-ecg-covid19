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

import javafx.beans.property.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.lang.ref.WeakReference;

import static de.qaware.ekg.awb.common.ui.bindings.ConversionBidirectionalBinding.getPropertyName;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the {@link ConversionBidirectionalBinding}.
 */
public class ConversionBidirectionalBindingTest {

    private IntegerProperty property1 = new SimpleIntegerProperty(0);
    private StringProperty property2 = new SimpleStringProperty("null");
    private StringProperty property2w = new SimpleStringProperty("null");
    private ConversionBidirectionalBinding<Number, String> binding;

    @Before
    public void setUp() throws Exception {
        binding = new ConversionBidirectionalBinding<>(property1, property2, Object::toString, Integer::parseInt);
        property1.addListener(binding);
        property2.addListener(binding);
    }

    @Test
    public void testSetFirst() throws Exception {
        property1.setValue(5);
        assertThat(property2.getValue(), is(equalTo("5")));

        ((WeakReference) Whitebox.getInternalState(binding, "propertyRef2W")).clear();
        property1.setValue(10);
        assertThat(property2.getValue(), is(equalTo("5")));
    }

    @Test
    public void testSetSecond() throws Exception {
        property2.setValue("10");
        assertThat(property1.getValue(), is(equalTo(10)));

        ((WeakReference) Whitebox.getInternalState(binding, "propertyRef1")).clear();
        property2.setValue("5");
        assertThat(property1.getValue(), is(equalTo(10)));
    }

    @Test
    public void testAsymetricBinding() throws Exception {
        property2 = new SimpleStringProperty();
        property2.setValue("0");
        ConversionBidirectionalBinding<Number, String> binding = new ConversionBidirectionalBinding<>(property1, property2, property2w, Object::toString, Integer::parseInt);
        property1.addListener(binding);
        property2.addListener(binding);

        property1.setValue(10);
        assertThat(property2w.get(), is(equalTo("10")));
        assertThat(property2.get(), is(equalTo("0")));

        property2.setValue("15");
        assertThat(property1.get(), is(equalTo(15)));
        assertThat(property2w.get(), is(equalTo("10")));

        property2w.addListener((o, ov, nv) -> property2.setValue(nv));
        property1.setValue(5);
        assertThat(property2.get(), is(equalTo("5")));
        assertThat(property2w.get(), is(equalTo("5")));

        property2.setValue("6");
        assertThat(property1.get(), is(equalTo(6)));
        assertThat(property2w.get(), is(equalTo("5")));
    }

    @Test
    public void testGetPropertyName() throws Exception {
        assertThat(getPropertyName(null), is(equalTo("")));
        assertThat(getPropertyName(new SimpleBooleanProperty(this, "")), is(equalTo("")));
        assertThat(getPropertyName(new SimpleBooleanProperty(null, "abc")), is(equalTo("")));
        assertThat(getPropertyName(new SimpleBooleanProperty(this, "abc")), endsWith(":abc"));
    }
}
