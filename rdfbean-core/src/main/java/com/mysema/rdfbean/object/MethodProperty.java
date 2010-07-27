/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BeanMap;

/**
 * @author sasa
 *
 */
@Immutable
public final class MethodProperty extends MappedProperty<Method> {

    @Nullable
    public static MethodProperty getMethodPropertyOrNull(Method method, MappedClass declaringClass) {
        try {
            return new MethodProperty(method, declaringClass);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getPropertyName(Method method) {
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (name.startsWith("is")
                && parameterTypes.length == 0 
                && ( returnType.equals(boolean.class) 
                        || returnType.equals(Boolean.class) )) {
            return Character.toString(Character.toLowerCase(name.charAt(2))) + name.substring(3); 
        } else if (name.startsWith("get") 
                && parameterTypes.length == 0 
                && !returnType.equals(void.class)) {
            return Character.toString(Character.toLowerCase(name.charAt(3))) + name.substring(4);
        } else if (name.startsWith("set") // allow method chaining by returning "this"
                && parameterTypes.length == 1) {
            return Character.toString(Character.toLowerCase(name.charAt(3))) + name.substring(4);
        } else {
            throw new IllegalArgumentException("Not getter or setter method: " + method);
        }
    }
    
    private final boolean getter;
    
    private final Method method;

    public MethodProperty(Method method, MappedClass declaringClass) {
        this(method, method.getAnnotations(), declaringClass);
    }
    
    public MethodProperty(Method method, Annotation[] annotations, MappedClass declaringClass) {
        super(getPropertyName(method), annotations, declaringClass);
        this.method = method;
        if (method.getName().startsWith("set")) {
            getter = false;
        } else {
            getter = true;
        }
    }

    @Override
    public Method getMember() {
        return method;
    }

    @Override
    protected Class<?> getTypeInternal() {
        if (getter) {
            return method.getReturnType();
        } else {
            return method.getParameterTypes()[0];
        }
    }

    @Override
    public Type getGenericType() {
        Type gtype = null;
        if (getter) {
            gtype = method.getGenericReturnType();
        } else {
            Type[] ptypes = method.getGenericParameterTypes();
            gtype = ptypes[0];
        }
        return gtype;
    }

    @Override
    public void setValue(BeanMap beanMap, Object value) {
        beanMap.put(getName(), value);
    }

    @Override
    public Object getValue(BeanMap instance) {
        return instance.get(getName());
    }

    @Override
    public boolean isVirtual() {
        return getSetter() == null;
    }
    
    @Nullable
    private Method getSetter() {
        Method setter = null;
        if (!getter) {
            setter = method;
        } else {
            Class<?> clazz = method.getDeclaringClass();
            Class<?> type = getType();
            if (boolean.class.equals(type) || Boolean.class.equals(type)) {
                try {
                    setter = clazz.getDeclaredMethod("is" + capitalize(getName()), type);
                } catch (SecurityException e) {
                    // ignore
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
            if (setter == null) {
                try {
                    setter = clazz.getDeclaredMethod("get" + capitalize(getName()), type);
                } catch (SecurityException e) {
                    // ignore
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
        }
        return setter;
    }
    
    private String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

}
