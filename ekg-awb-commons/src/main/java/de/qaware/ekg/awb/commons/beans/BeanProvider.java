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
package de.qaware.ekg.awb.commons.beans;

import de.qaware.ekg.awb.sdk.core.lookup.EkgLookup;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * his class contains utility methods for resolution of contextual references in situations where no injection is
 * available because the current class is not managed by the CDI Container.
 * <p>
 * <b>Attention:</b> This approach is intended for use in user code at runtime. If BeanProvider is used during Container
 * boot (in an Extension), non-portable behaviour results. The CDI specification only allows injection of the
 * BeanManager during CDI container boot time.</p>
 * <p>
 * The methods are adopted from apache delta spike.
 * See <a href="https://github.com/apache/deltaspike/blob/7ad36c14c22df3bb0b156a397766af29892f0bcf/deltaspike/core/api/src/main/java/org/apache/deltaspike/core/api/provider/BeanProvider.java">BeanProvider.java</a>
 * for more information.
 */
public final class BeanProvider {

    private BeanProvider() {
    }

    /**
     * Performs dependency injection on an instance. Useful for instances which aren't managed by CDI.
     * <p>
     * <b>Attention:</b><br/>
     * The resulting instance isn't managed by CDI; only fields annotated with @Inject get initialized.
     *
     * @param instance current instance
     * @param <T>      current type
     * @return instance with injected fields (if possible - or null if the given instance is null)
     */
    @SuppressWarnings("unchecked")
    public static <T> T injectFields(T instance) {
        if (instance == null) {
            return null;
        }

        BeanManager beanManager = getBeanManager();

        CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType<T> annotatedType = beanManager.createAnnotatedType((Class<T>) instance.getClass());
        InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(instance, creationalContext);
        return instance;
    }

    private static BeanManager getBeanManager() {
        return EkgLookup.lookup(BeanManager.class);
    }
}
