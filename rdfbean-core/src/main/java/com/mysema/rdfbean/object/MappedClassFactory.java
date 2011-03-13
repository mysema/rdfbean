/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Context;
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;

/**
 * MappedClassFactory provides a factory for MappedClass creation
 *
 * @author tiwe
 * @version $Id$
 */
public class MappedClassFactory {

    private final Map<Class<?>, MappedClass> mappedClasses = new LinkedHashMap<Class<?>, MappedClass>();

    @Nullable
    private final String defaultNamespace;

    public MappedClassFactory(@Nullable String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    private void assignConstructor(Class<?> clazz, MappedClass mappedClass) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            return;
        }
        
        ConstructorVisitor visitor = new ConstructorVisitor();        
        try {
            if (clazz.getClassLoader() != null){
                InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/')+".class");
                ClassReader cr = new ClassReader(is);
                cr.accept(visitor, 0);    
            }            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<Integer, List<String>> paramsMap = new HashMap<Integer, List<String>>();
        for (List<String> c : visitor.getConstructors()){
            paramsMap.put(c.size(), c);
        }
        
        MappedConstructor defaultConstructor = null;
        MappedConstructor mappedConstructor = null;
        nextConstructor: for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                defaultConstructor = new MappedConstructor(constructor);
            } else {
                List<MappedPath> mappedArguments = new ArrayList<MappedPath>();
                List<String> params = paramsMap.get(constructor.getParameterTypes().length);
                if (params == null){
                    continue;
                }
                for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                    MappedPath mappedPath = getPathMapping(mappedClass, constructor, i, params.get(i));
                    if (mappedPath != null) {
                        mappedArguments.add(mappedPath);
                    } else if (mappedArguments.size() > 0) {
                        throw new IllegalArgumentException("Constructor has unmapped parameters: " + constructor);
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
                path = getPathMapping(classNs, field, mappedClass);
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
            path = getPathMapping(classNs, method, mappedClass);
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
    private MappedPath getMappedPath(MappedProperty<?> property, @Nullable List<MappedPredicate> path) {
        property.resolve(null);
        if (path != null) {
            return new MappedPath(property, path, false);
        } else {
            if (property.isAnnotatedProperty()) {
                return new MappedPath(property, Collections.<MappedPredicate>emptyList(), false);
            } else {
                return null;
            }
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

    @Nullable
    private MappedPath getPathMapping(MappedClass mappedClass, Constructor<?> constructor, int parameterIndex, String property) {
        boolean reference = mappedClass.hasProperty(property);
        ConstructorParameter constructorParameter = new ConstructorParameter(constructor, parameterIndex, mappedClass, reference ? property : null);
        if (constructorParameter.isPropertyReference()) {
            return mappedClass.getMappedPath(property);
        } else {
            List<MappedPredicate> path = getPredicatePath(mappedClass.getClassNs(), constructorParameter);
            return getMappedPath(constructorParameter, path);
        }
    }


    private MappedPath getPathMapping(String classNs, Field field, MappedClass declaringClass) {
        FieldProperty property = new FieldProperty(field, declaringClass);
        List<MappedPredicate> path = getPredicatePath(classNs, property);
        return getMappedPath(property, path);
    }

    @Nullable
    private MappedPath getPathMapping(String classNs, Method method, MappedClass declaringClass) {
        MethodProperty property = MethodProperty.getMethodPropertyOrNull(method, declaringClass);
        if (property != null) {
            List<MappedPredicate> path = getPredicatePath(classNs, property);
            return getMappedPath(property, path);
        } else {
            return null;
        }
    }

    @Nullable
    private List<MappedPredicate> getPredicatePath(String classNs, MappedProperty<?> property) {
        String parentNs = classNs;
        Path path = property.getAnnotation(Path.class);
        Predicate[] predicates;
        if (path != null) {
            if (StringUtils.isNotEmpty(path.ns())) {
                parentNs = path.ns();
            }
            predicates = path.value();
        } else {
            Predicate predicate = property.getAnnotation(Predicate.class);
            if (predicate != null) {
                predicates = new Predicate[] { predicate };
            } else {
                predicates = null;
            }
        }
        if (predicates != null) {
            List<MappedPredicate> predicatePath =
                new ArrayList<MappedPredicate>(predicates.length);
            boolean first = true;
            for (Predicate predicate : predicates) {
                predicatePath.add(
                        new MappedPredicate(parentNs, predicate,
                                first ? property.getName() : null));
                first = false;
            }
            return predicatePath;
        } else {
            return null;
        }
    }

    @Nullable
    private UID getUID(Class<?> clazz) {
        ClassMapping cmap = clazz.getAnnotation(ClassMapping.class);
        if (cmap != null) {
            String ns = cmap.ns();
            if (StringUtils.isEmpty(ns)){
                ns = defaultNamespace;
            }
            String ln = cmap.ln();
            if (StringUtils.isEmpty(ln)){
                ln = clazz.getSimpleName();
            }
            if (ns != null){
                return new UID(ns, ln);
            }else{
                throw new IllegalArgumentException("Namespace needs to be declared in ClassMapping or configuration.");
            }
        } else {
            // NOTE : might be used for autowire etc, doesn't need ClassMapping for such cases
            return null;
        }
    }

    private boolean isProcessedClass(Class<?> clazz) {
        Package pack = clazz.getPackage();
        return pack == null || !pack.getName().startsWith("java");
    }

}
