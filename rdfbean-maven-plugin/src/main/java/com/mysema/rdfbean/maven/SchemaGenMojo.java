/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.maven;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jboss.util.file.ArchiveBrowser;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.identity.MemoryIdentityService;
import com.mysema.rdfbean.sesame.SesameSchemaGen;

/**
 * SchemaGenMojo provides a Maven plugin for Sesame based schema generation
 *
 * @author tiwe
 * @version $Id$
 * 
 * @goal schemagen
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class SchemaGenMojo extends BaseMojo{
        
    /** @parameter required=true */
    private String namespace;
    
    /** @parameter required=true */
    private String prefix;
    
    /** @parameter */
    private String ontology;
    
    /** @parameter required=true */
    private File targetFile;
    
    private ArchiveBrowser.Filter filter = new ArchiveBrowser.Filter() {
        private final Pattern pattern = Pattern.compile(".*\\.class");
        public boolean accept(String name) {
            return pattern.matcher(name).matches();
        }
    };
    

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            URLClassLoader classLoader = getProjectClassLoader();
            List<Class<?>> entityClasses = getEntityClasses(classLoader);
            DefaultConfiguration configuration = new DefaultConfiguration();
            configuration.addClasses(entityClasses.toArray(new Class[entityClasses.size()]));
            configuration.setIdentityService(MemoryIdentityService.instance());
            
            if (ontology == null){
                if (namespace.endsWith("#")){
                    ontology = namespace.substring(0, namespace.length()-1);
                }else{
                    ontology = namespace;
                }
            }
            
            new SesameSchemaGen()
            .setNamespace(prefix, namespace)
            .setOntology(ontology)
            .setOutputStream(new FileOutputStream(targetFile))
            .addExportNamespace(namespace)
            .generateRDFXML(configuration);
            
        } catch (Exception e) {
            if (e instanceof RuntimeException){
                throw (RuntimeException)e;
            }else{
                throw new RuntimeException(e);    
            }            
        }               
    }

    private List<Class<?>> getEntityClasses(URLClassLoader classLoader) throws IOException, 
        ClassNotFoundException {

        List<Class<?>> entityClasses = new ArrayList<Class<?>>();
        
        for (URL url : classLoader.getURLs()){
            Iterator<?> classes = ArchiveBrowser.getBrowser(url, filter);
            while (classes.hasNext()){
                ClassFile classFile = null;
                DataInputStream in = null;
                try{
                    in = new DataInputStream((InputStream)classes.next());
                    classFile = new ClassFile(in);
                }finally{
                    if (in != null){
                        in.close();
                    }
                }
                AnnotationsAttribute annotations = (AnnotationsAttribute) 
                    classFile.getAttribute(AnnotationsAttribute.visibleTag);
                if (annotations != null && annotations.getAnnotation(ClassMapping.class.getName()) != null){
                    Class<?> clazz = Class.forName(classFile.getName(), true, classLoader);
                    entityClasses.add(clazz);
                }
                
            }
        }
        return entityClasses;
    }

    

}
