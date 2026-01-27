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
package de.qaware.ekg.awb.commons.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Some reflection helper to deal with methods.
 */
public final class MethodHelper {
    private static Map<String, Method> methodCache = new HashMap<>();

    private MethodHelper() {
    }

    /**
     * Invoke a private method on given object.
     *
     * @param object     The object for calling the method.
     * @param methodName The method name.
     * @param params     The parameter values to invoke the method.
     * @param <T>        The expected return type of the invoked method.
     * @return The return value of invoked method or null if execution fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = Arrays.stream(params)
                    .map(Object::getClass)
                    .collect(Collectors.toList())
                    .toArray(new Class<?>[]{});
            Method method = findMethod(object.getClass(), methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(object, params);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Can not invoke method \"" + methodName + "\" on object of class \""
                    + object.getClass().getSimpleName() + "\".", e);
        }
    }

    /**
     * Find a declared method with name {@code methodName} and the given parameter
     * types in the given {@code clazz} or one of it's super classes.
     *
     * @param clazz      The class to start the search.
     * @param methodName The requested method name
     * @param paramTypes The wanted parameter types of the requested method.
     * @return The found method.
     * @throws NoSuchMethodException In case of the method can not be found in the given
     *                               search class or one of it's super classes.
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        String key = clazz.getName() + "#" + methodName + "(" +
                Stream.of(paramTypes).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        }
        NoSuchMethodException exception = null;
        Class<?> cls = clazz;
        while (cls != null) {
            try {
                Method method = cls.getDeclaredMethod(methodName, paramTypes);
                methodCache.put(key, method);
                return method;
            } catch (NoSuchMethodException e) {
                if (exception == null) {
                    exception = e;
                }
            }
            cls = cls.getSuperclass();
        }
        throw exception;
    }
}
