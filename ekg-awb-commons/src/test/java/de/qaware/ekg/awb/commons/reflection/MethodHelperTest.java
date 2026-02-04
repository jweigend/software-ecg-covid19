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
package de.qaware.ekg.awb.commons.reflection;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;


/**
 * Unit test for {@link MethodHelper}.
 */
public class MethodHelperTest {

    @Test
    public void testInvokeMethod() throws Exception {
        Pattern pattern = Pattern.compile("(?<a>.*)aaa");

        Map<String, Integer> groups = MethodHelper.invokeMethod(pattern, "namedGroups");
        assertThat(groups, hasKey("a"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInvokeMethodFails() throws Exception {
        Pattern pattern = Pattern.compile(".*aaa");

        MethodHelper.invokeMethod(pattern, "namedGroup");
    }

    @Test
    public void testFindMethod() throws Exception {
        Method expected = ValueAxis.class.getDeclaredMethod("scalePropertyImpl");

        assertThat(MethodHelper.findMethod(ValueAxis.class, "scalePropertyImpl"), is(expected));
        assertThat(MethodHelper.findMethod(NumberAxis.class, "scalePropertyImpl"), is(expected));
        assertThat(MethodHelper.findMethod(NumberAxis.class, "scalePropertyImpl"), is(expected));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testFindMethodNoMethodFound() throws Exception {
        MethodHelper.findMethod(NumberAxis.class, "MethodThatDoNotExists");
    }
}
