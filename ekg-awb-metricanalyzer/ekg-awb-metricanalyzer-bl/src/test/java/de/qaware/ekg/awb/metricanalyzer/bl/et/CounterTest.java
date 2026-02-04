//______________________________________________________________________________
//
//          Project:    Software EKG
//______________________________________________________________________________
//
//         Author:      Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.metricanalyzer.bl.et;

import de.qaware.ekg.awb.sdk.datamodel.Counter;
import de.qaware.ekg.awb.sdk.datamodel.Value;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for the {@link Counter}.
 */
public class CounterTest {

    @Test
    public void testGetCounterName() {
        Counter counter = new Counter("This_is_my_name");
        String counterName = counter.getCounterName();

        assertThat(counterName, is(equalTo("This_is_my_name")));
    }

    @Test
    public void testGetValuesWithoutAddingValuesBefore() {
        Counter counter = new Counter("This_is_my_name");
        List<Value> values = counter.getValues();

        assertThat(values.size(), is(equalTo(0)));
    }

    @Test
    public void testGetValuesWithOneAddedValue() {
        Counter counter = new Counter("This_is_my_name");
        Value valueToAdd = new Value(Instant.now().toEpochMilli(), 1.0);
        counter.addValue(valueToAdd);
        List<Value> values = counter.getValues();

        assertThat(values.size(), is(equalTo(1)));

        Value firstValue = values.get(0);
        assertThat(valueToAdd, is(equalTo(firstValue)));
    }

}
