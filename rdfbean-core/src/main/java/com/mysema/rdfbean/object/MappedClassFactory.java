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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mysema.rdfbean.model.UID;

/**
 * MappedClassFactory provides a factory for MappedClass creation
 * 
 * @author tiwe
 * @version $Id$
 */
public final class MappedClassFactory {

    private MappedClassFactory() {
    }

    private static Map<Class<?>, MappedClass> mappedClasses = Collections
            .synchronizedMap(new LinkedHashMap<Class<?>, MappedClass>());

    private static void assignConstructor(Class<?> clazz,
            MappedClass mappedClass) {
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

    private static void collectFieldPaths(Class<?> clazz,
            MappedClass mappedClass) {
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

    private static void collectDynamicFieldProperties(Class<?> clazz,
            MappedClass mappedClass) {
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

    private static void collectMethodPaths(Class<?> clazz,
            MappedClass mappedClass) {
        MappedPath path;
        String classNs = MappedClass.getClassNs(clazz);
        for (Method method : clazz.getDeclaredMethods()) {
            path = MappedPath.getPathMapping(classNs, method, mappedClass);
            if (path != null) {
                mappedClass.addMappedPath(path);
            }
        }
    }

    public static MappedClass getMappedClass(Class<?> clazz) {
        // NOTE: no need to further synchronize access to mappedPaths because
        // result is immutable and deterministic, i.e. it does't really matter
        // if it gets calculated multiple times
        MappedClass mappedClass = mappedClasses.get(clazz);
        if (mappedClass == null) {
            mappedClass = new MappedClass(clazz);
            if (!clazz.isEnum()) {
                for (MappedClass mappedSuperClass : mappedClass
                        .getMappedSuperClasses()) {
                    if (mappedSuperClass != null) {
                        for (MappedPath path : mappedSuperClass.getProperties()) {
                            MappedProperty<?> property = (MappedProperty<?>) path
                                    .getMappedProperty().clone();
                            property.resolve(mappedClass);
                            mappedClass.addMappedPath(new MappedPath(property,
                                    path.getPredicatePath(), !mappedClass
                                            .equals(property
                                                    .getDeclaringClass())));
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

}
