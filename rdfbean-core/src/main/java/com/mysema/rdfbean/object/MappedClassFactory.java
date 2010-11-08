/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.model.UID;

/**
 * MappedClassFactory provides a factory for MappedClass creation
 * 
 * @author tiwe
 * @version $Id$
 */
public class MappedClassFactory {

    private final Map<Class<?>, MappedClass> mappedClasses = new LinkedHashMap<Class<?>, MappedClass>();

    private void assignConstructor(Class<?> clazz, MappedClass mappedClass) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            return;
        }
        MappedConstructor defaultConstructor = null;
        MappedConstructor mappedConstructor = null;
        nextConstructor: for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = new MappedConstructor(constructor);
            } else {
                List<MappedPath> mappedArguments = new ArrayList<MappedPath>();
                for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                    MappedPath mappedPath = MappedPath.getPathMapping(
                            mappedClass, constructor, i);
                    if (mappedPath != null) {
                        mappedArguments.add(mappedPath);
                    } else if (mappedArguments.size() > 0) {
                        throw new IllegalArgumentException(
                                "Constructor has unmapped parameters: "
                                        + constructor);
                    } else {
                        continue nextConstructor;
                    }
                }
                if (!mappedArguments.isEmpty()) {
                    if (mappedConstructor != null) {
                        throw new IllegalArgumentException(
                                "Ambiguous mapped constructor: " + constructor);
                    } else {
                        mappedConstructor = new MappedConstructor(constructor,
                                mappedArguments);
                    }
                }
            }
        }
        if (mappedConstructor != null) {
            mappedClass.setMappedConstructor(mappedConstructor);
        } else if (defaultConstructor != null) {
            mappedClass.setMappedConstructor(defaultConstructor);
        }
        // else {
        // // TODO return false?
        // }
    }

    private void collectDynamicFieldProperties(Class<?> clazz, MappedClass mappedClass) {
        if (!clazz.isInterface()) {
            for (Field field : clazz.getDeclaredFields()) {
                FieldProperty property = new FieldProperty(field, mappedClass);
                if (property.isDynamic()) {
                    property.resolve(null);

                    if (!property.isMap()) {
                        throw new IllegalArgumentException(
                                "Only properties type of java.util.Map, can be annotated with @Properties");
                    }
                    if (!UID.class.equals(property.getKeyType())) {
                        throw new IllegalArgumentException(
                                "Key must be type of com.mysema.rdfbean.model.UID");
                    }
                    else {
                        mappedClass.addDynamicProperty(property);
                    }
                }
            }
        }
    }

    private void collectFieldPaths(Class<?> clazz, MappedClass mappedClass) {
        if (!clazz.isInterface()) {
            MappedPath path;
            String classNs = MappedClass.getClassNs(clazz);
            for (Field field : clazz.getDeclaredFields()) {
                path = MappedPath.getPathMapping(classNs, field, mappedClass);
                if (path != null) {
                    mappedClass.addMappedPath(path);
                }
            }
        }
    }

    private void collectMethodPaths(Class<?> clazz, MappedClass mappedClass) {
        MappedPath path;
        String classNs = MappedClass.getClassNs(clazz);
        for (Method method : clazz.getDeclaredMethods()) {
            path = MappedPath.getPathMapping(classNs, method, mappedClass);
            if (path != null) {
                mappedClass.addMappedPath(path);
            }
        }
    }

    public MappedClass getMappedClass(Class<?> clazz) {
        // NOTE: no need to further synchronize access to mappedPaths because
        // result is immutable and deterministic, i.e. it does't really matter
        // if it gets calculated multiple times
        MappedClass mappedClass = mappedClasses.get(clazz);
        if (mappedClass == null) {
            UID uid = getUID(clazz);
            Context context = clazz.getAnnotation(Context.class);
            List<MappedClass> superclasses = getMappedSuperClasses(clazz);
            mappedClass = new MappedClass(clazz, uid, context != null ? new UID(context.value()) : null, superclasses);
            if (!clazz.isEnum()) {
                for (MappedClass mappedSuperClass : mappedClass.getMappedSuperClasses()) {
                    if (mappedSuperClass != null) {
                        for (MappedPath path : mappedSuperClass.getProperties()) {
                            MappedProperty<?> property = (MappedProperty<?>) path.getMappedProperty().clone();
                            property.resolve(mappedClass);
                            mappedClass.addMappedPath(new MappedPath(property,
                                    path.getPredicatePath(), !mappedClass.equals(property.getDeclaringClass())));
                        }
                    }
                }

                // Collect direct properties (merge with super properties)
                collectFieldPaths(clazz, mappedClass);
                collectMethodPaths(clazz, mappedClass);
                collectDynamicFieldProperties(clazz, mappedClass);
                assignConstructor(clazz, mappedClass);
                mappedClass.close();
                mappedClasses.put(clazz, mappedClass);
            }
        }
        return mappedClass;
    }
    
    @Nullable
    private static UID getUID(Class<?> clazz) {
        ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            if (StringUtils.isNotEmpty(cmap.ln())) {
                return new UID(cmap.ns(), cmap.ln());
            } else if (StringUtils.isNotEmpty(cmap.ns())) {
                return new UID(cmap.ns(), clazz.getSimpleName());
            } else {
                // if ClassMapping is used, then either ns or ln should be given, eitherwise the ClassMapping is incomplete
                throw new IllegalArgumentException("Both ns and ln are empty for " + clazz.getName());
            }
        } else {
            // NOTE : might be used for autowire etc, doesn't need ClassMapping for such cases
            return null;
        }
    }
    
    private List<MappedClass> getMappedSuperClasses(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        Class<?>[] ifaces = clazz.getInterfaces();
        List<MappedClass> mappedSuperClasses = new ArrayList<MappedClass>(ifaces != null ? ifaces.length + 1 : 1);
        if (superClass != null && !Object.class.equals(superClass)) {
            if (isProcessedClass(superClass)) {
                mappedSuperClasses.add(getMappedClass(superClass));
            }
        }
        if (ifaces != null) {
            for (Class<?> iface : ifaces) {
                if (isProcessedClass(iface)) {
                    mappedSuperClasses.add(getMappedClass(iface));
                }
            }
        }
        return mappedSuperClasses;
    }
    
    private static boolean isProcessedClass(Class<?> clazz) {
        Package pack = clazz.getPackage();
        return pack == null || !pack.getName().startsWith("java");
    }
    

}
