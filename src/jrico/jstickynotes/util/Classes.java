package jrico.jstickynotes.util;

import java.lang.reflect.Method;

public class Classes {

    public static final int PUBLIC = 0;
    public static final int ALL_CLASS = 1;
    public static final int ALL_RECURSIVE = 2;

    /**
     * 
     * @param objects
     * @return
     */
    public static Class<?>[] getTypes(Object... objects) {
        Class<?>[] parameterTypes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            parameterTypes[i] = objects[i].getClass();
        }
        return parameterTypes;
    }

    /**
     * 
     * @param type
     * @param name
     * @param searchMode
     * @param parameterTypes
     * @return
     */
    public static Method getMethod(Class<?> type, String name, int searchMode, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = type.getMethod(name, parameterTypes);
        } catch (Exception e) {
            if (searchMode == ALL_CLASS) {
                try {
                    method = type.getDeclaredMethod(name, parameterTypes);
                } catch (Exception ex) {
                    // do nothing
                }
            } else if (searchMode == ALL_RECURSIVE) {
                method = findMethod(type, name, parameterTypes);
            }
        }
        return method;
    }

    /**
     * 
     * @param type
     * @param name
     * @param parameterTypes
     * @return
     */
    public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        return getMethod(type, name, PUBLIC, parameterTypes);
    }

    /**
     * 
     * @param <T>
     * @param object
     * @param name
     * @param searchMode
     * @param parameters
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, String name, int searchMode, Object... parameters) {
        Method method = getMethod(object.getClass(), name, searchMode, getTypes(parameters));
        return (T) invokeMethod(object, method, parameters);
    }

    /**
     * 
     * @param <T>
     * @param object
     * @param method
     * @param parameters
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, Method method, Object... parameters) {
        T result = null;
        if (method != null) {
            boolean accessible = method.isAccessible();
            try {
                try {
                    if (!accessible) {
                        method.setAccessible(true);
                    }
                    result = (T) method.invoke(object, parameters);
                } finally {
                    if (!accessible) {
                        method.setAccessible(false);
                    }
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * 
     * @param <T>
     * @param object
     * @param name
     * @param parameters
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object object, String name, Object... parameters) {
        return (T) invokeMethod(object, name, PUBLIC, parameters);
    }

    /**
     * 
     * @param type
     * @param property
     * @return
     */
    public static Method getGetter(Class<?> type, String property) {
        Method getter = getMethod(type, getGetterName(property, false));
        if (getter == null) {
            getter = getMethod(type, getGetterName(property, true));
        }
        return getter;
    }

    /**
     * 
     * @param type
     * @param property
     * @param valueType
     * @return
     */
    public static Method getSetter(Class<?> type, String property, Class<?> valueType) {
        return getMethod(type, getSetterName(property), valueType);
    }

    /**
     * 
     * @param type
     * @param adderName
     * @param valueType
     * @return
     */
    public static Method getAdder(Class<?> type, String adderName, Class<?> valueType) {
        return getMethod(type, adderName, valueType);
    }

    /**
     * 
     * @param type
     * @param removerName
     * @param valueType
     * @return
     */
    public static Method getRemover(Class<?> type, String removerName, Class<?> valueType) {
        return getMethod(type, removerName, valueType);
    }

    /**
     * Looks for an <code>updater</code> method (<code>setter</code> or <code>adder</code> or <code>remover</code>) of a
     * property.
     * 
     * @param type
     * @param property
     * @param valueType
     * @return
     */
    public static Method getUpdater(Class<?> type, String property, Class<?> valueType) {
        Method updater = getSetter(type, property, valueType);
        if (updater == null) {
            updater = getAdder(type, property, valueType);
            if (updater == null) {
                updater = getRemover(type, property, valueType);
            }
        }
        return updater;
    }

    /**
     * Returns the <code>getter</code> form of a property.
     * 
     * @param property
     *            the property name.
     * @return the <code>getter</code> name.
     */
    public static String getGetterName(String property, boolean isBoolean) {
        char firstLetter = Character.toUpperCase(property.charAt(0));
        String setterName = (isBoolean ? "is" : "get") + firstLetter + property.substring(1);
        return setterName;
    }

    /**
     * Returns the <code>setter</code> form of a property.
     * 
     * @param property
     *            the property name.
     * @return the <code>setter</code> name.
     */
    public static String getSetterName(String property) {
        char firstLetter = Character.toUpperCase(property.charAt(0));
        String setterName = "set" + firstLetter + property.substring(1);
        return setterName;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = type.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            // do nothing
        }
        if (method == null && type.getSuperclass() != null) {
            method = findMethod(type.getSuperclass(), name, parameterTypes);
        }
        return method;
    }

}
