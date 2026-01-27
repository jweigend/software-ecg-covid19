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
package de.qaware.ekg.awb.repository.bl;

import de.qaware.ekg.awb.repository.api.EkgRepository;
import de.qaware.ekg.awb.repository.api.events.RepositoryChangeEvent;
import de.qaware.ekg.awb.sdk.core.events.ChangedEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link RepositoryChangeEvent}
 */
public class EkgRepositoryChangeEventTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullAsSource() {
        new RepositoryChangeEvent(mock(EkgRepository.class), ChangedEvent.Change.UPDATE, null);
    }

    @Test
    public void testObjectNull() {
        RepositoryChangeEvent event = new RepositoryChangeEvent(null, ChangedEvent.Change.UPDATE, this);
        assertThat(event.getObj(), is(nullValue()));
    }

    @Test
    public void testChangeNull() {
        RepositoryChangeEvent event = new RepositoryChangeEvent(mock(EkgRepository.class), null, this);
        assertThat(event.getChange(), is(nullValue()));
    }

    @Test
    public void testGetObj() {
        EkgRepository mock = mock(EkgRepository.class);
        RepositoryChangeEvent event = new RepositoryChangeEvent(mock, ChangedEvent.Change.UPDATE, this);
        MatcherAssert.assertThat(event.getChange(), Matchers.is(ChangedEvent.Change.UPDATE));
        assertThat(event.getObj(), is(equalTo(mock)));
    }
}
