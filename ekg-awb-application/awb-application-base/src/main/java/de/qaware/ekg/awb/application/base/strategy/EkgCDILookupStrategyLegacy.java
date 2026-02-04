//______________________________________________________________________________
//
//                  ProjectConfiguration:    Software EKG
//______________________________________________________________________________
//
//                   Author:    Weigend AM GmbH & Co KG 2009 - 2025
//______________________________________________________________________________
//
// Notice: This piece of software was created, designed and implemented by
// experienced craftsmen and innovators in Munich, Germany.
// Changes should be done with respect to the original design.
//______________________________________________________________________________
package de.qaware.ekg.awb.application.base.strategy;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;

import javax.enterprise.util.TypeLiteral;
import javax.inject.Singleton;
import java.util.List;

/**
 * Adapter for LookupStrategy in stage diver and awb
 */
@Singleton
public class EkgCDILookupStrategyLegacy {
    private LookupStrategy lookupStrategy = Lookup.getLookupStrategy();

    public <T> T lookup(Class<T> clazz) {
        return lookupStrategy.lookup(clazz);
    }

    public <T> T lookup(TypeLiteral<T> type) {
        return lookupStrategy.lookup(type);
    }

    public <T> List<T> lookupAll(TypeLiteral<T> type) {
        return lookupStrategy.lookupAll(type);
    }

    public <T> List<T> lookupAll(Class<T> clazz) {
        return lookupStrategy.lookupAll(clazz);
    }
}
