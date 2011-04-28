/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.MappedClasses;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * Default implementation of the Configuration interface
 *
 * @author sasa
 *
 */
public class DefaultConfiguration implements Configuration {

    private static final Pattern JAR_URL_SEPARATOR = Pattern.compile("!");

    private static final Set<String> buildinNamespaces = new HashSet<String>();

    static {
        buildinNamespaces.add(RDF.NS);
        buildinNamespaces.add(RDFS.NS);
        buildinNamespaces.add(XSD.NS);
        buildinNamespaces.add(OWL.NS);
        buildinNamespaces.add(CORE.NS);
    }

    private final Set<MappedClass> mappedClasses = new LinkedHashSet<MappedClass>();

    private final Set<Class<?>> polymorphicClasses = new HashSet<Class<?>>();

    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();

    private final MappedClassFactory mappedClassFactory;

    private final Set<String> restrictedResources = new HashSet<String>(buildinNamespaces);

    private final Map<UID, List<MappedClass>> type2classes = new HashMap<UID, List<MappedClass>>();

    public DefaultConfiguration(@Nullable String defaultNamespace) {
        this.mappedClassFactory = new MappedClassFactory(defaultNamespace);
    }

    public DefaultConfiguration() {
        this((String)null);
    }

    public DefaultConfiguration(Class<?>... classes) {
        this((String)null);
        addClasses(classes);
    }

    public DefaultConfiguration(Package... packages) {
        this((String)null);
        addPackages(packages);
    }

    public DefaultConfiguration(@Nullable String defaultNamespace, Class<?>... classes) {
        this(defaultNamespace);
        addClasses(classes);
    }

    public DefaultConfiguration(@Nullable String defaultNamespace, Package... packages) {
        this(defaultNamespace);
        addPackages(packages);
    }

    public final void addClasses(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.getAnnotation(ClassMapping.class) != null){
                MappedClass mappedClass = mappedClassFactory.getMappedClass(clazz);
                if (mappedClass.getUID() != null) {
                    List<MappedClass> classList = type2classes.get(mappedClass.getUID());
                    if (classList == null) {
                        classList = new ArrayList<MappedClass>();
                        type2classes.put(mappedClass.getUID(), classList);
                    }
                    classList.add(mappedClass);
                }
                for (MappedClass superClass : mappedClass.getMappedSuperClasses()){
                    polymorphicClasses.add(superClass.getJavaClass());
                }
                mappedClasses.add(mappedClass);
            }else{
                throw new IllegalArgumentException("No @ClassMapping annotation for " + clazz.getName());
            }
        }
    }

    public final void addPackages(Package... packages) {
        for (Package pack : packages) {
            MappedClasses classes = pack.getAnnotation(MappedClasses.class);
            if (classes != null) {
                addClasses(classes.value());
            }else{
                throw new IllegalArgumentException("No @MappedClasses annotation for " + pack.getName());
            }
        }
    }

    @Override
    public boolean allowCreate(Class<?> clazz) {
        return true;
    }

    public boolean allowRead(MappedPath path) {
        // TODO filter unmapped types?
        return true;
    }

    @Override
    @Nullable
    public UID createURI(Object instance) {
        Class<?> clazz = instance.getClass();
        UID context = getMappedClass(clazz).getContext();
        if (context != null) {
            return new UID(context.getId() + "#", clazz.getSimpleName() + "-" + UUID.randomUUID().toString());
        }
        return null;
    }

    @Override
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    @Override
    public MappedClass getMappedClass(Class<?> javaClass) {
        return mappedClassFactory.getMappedClass(javaClass);
    }

    @Override
    public Set<MappedClass> getMappedClasses() {
        return mappedClasses;
    }

    public List<MappedClass> getMappedClasses(UID uid) {
        if (type2classes.containsKey(uid)){
            return type2classes.get(Assert.notNull(uid,"uid"));
        }else{
            return Collections.emptyList();
        }
    }

    public boolean isMapped(Class<?> clazz){
        return clazz.getAnnotation(ClassMapping.class) != null;
    }

    public boolean isPolymorphic(Class<?> clazz){
        return polymorphicClasses.contains(clazz);
    }

    @Override
    public boolean isRestricted(UID uid) {
        return restrictedResources.contains(uid.getId()) || restrictedResources.contains(uid.ns());
    }

    public void scanPackages(Package... packages){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (Package pkg : packages){
            try {
                for (Class<?> cl : scanPackage(classLoader, pkg)){
                    if (cl.getAnnotation(ClassMapping.class) != null){
                        addClasses(cl);
                    }
                }
            } catch (IOException e) {
                throw new ConfigurationException(e);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException(e);
            }
        }
    }
    
    Set<Class<?>> scanPackage(ClassLoader classLoader, Package pkg) throws IOException, ClassNotFoundException {
        Enumeration<URL> urls = classLoader.getResources(pkg.getName().replace('.', '/'));
        Set<Class<?>> classes = new HashSet<Class<?>>();
        while (urls.hasMoreElements()){
            URL url = urls.nextElement();
            if (url.getProtocol().equals("jar")){                
                try {
                    String[] fileAndPath = JAR_URL_SEPARATOR.split(url.getFile());
                    JarFile jarFile = new JarFile(new File(new URI(fileAndPath[0])));
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()){
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class") && entry.getName().startsWith(fileAndPath[1].substring(1))){
                            String className = entry.getName().substring(0, entry.getName().length()-6).replace('/', '.');
                            classes.add(Class.forName(className));
    
                        }
                    }
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }

            }else if (url.getProtocol().equals("file")){
                Deque<File> files = new ArrayDeque<File>();
                String packagePath;
                try {
                    File packageAsFile = new File(url.toURI());
                    packagePath = packageAsFile.getPath();
                    files.add(packageAsFile);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
                while (!files.isEmpty()){
                    File file = files.pop();
                    for (File child : file.listFiles()){
                        if (child.getName().endsWith(".class")){
                            String fileName = child.getPath().substring(packagePath.length()+1).replace(File.separatorChar, '.');
                            String className = pkg.getName() + "." + fileName.substring(0, fileName.length()-6);
                            classes.add(Class.forName(className));
                        }else if (child.isDirectory()){
                            files.add(child);
                        }
                    }
                }

            }else{
                throw new IllegalArgumentException("Illegal url : " + url);
            }
        }
        return classes;
    }


}
