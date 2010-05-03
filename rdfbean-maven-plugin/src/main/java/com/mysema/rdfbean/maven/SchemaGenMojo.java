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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jboss.util.file.ArchiveBrowser;
import org.openrdf.OpenRDFException;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.object.DefaultConfiguration;
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
public class SchemaGenMojo extends AbstractMojo{
        
    private static final Comparator<Class<?>> fileComparator = new Comparator<Class<?>>(){
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }                
    };
    
    /** 
     * @parameter 
     */
    private String classes;
    
    /** 
     * @parameter 
     */
    private File classListFile;
    
    /** 
     * @parameter required=true 
     */
    private String namespace;
    
    /** 
     * @parameter 
     */
    private String ontology;
    
    /** 
     * @parameter required=true 
     */
    private String prefix;
    
    /** 
     * @parameter expression="${project}" readonly=true required=true 
     */
    private MavenProject project;
    
    /** 
     * @parameter  
     */
    private File schemaFile;
        
    /** 
     * @parameter 
     */
    private boolean useTurtle;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            URLClassLoader classLoader = getProjectClassLoader();            
            final Pattern pattern = Pattern.compile(".*"+toRegex(classes)+"\\.class"); 
            ArchiveBrowser.Filter filter = new ArchiveBrowser.Filter() {
                public boolean accept(String name) {
                    return pattern.matcher(name).matches();
                }
            };
            List<Class<?>> entityClasses = getEntityClasses(classLoader, filter);
            Collections.sort(entityClasses, fileComparator);
            DefaultConfiguration configuration = new DefaultConfiguration();
            configuration.addClasses(entityClasses.toArray(new Class[entityClasses.size()]));
            
            if (ontology == null){
                if (namespace.endsWith("#") || namespace.endsWith("/")){
                    ontology = namespace.substring(0, namespace.length()-1);
                }else{
                    ontology = namespace;
                }
            }
            
            if (schemaFile != null){
                if (!schemaFile.getParentFile().exists()){
                    if (!schemaFile.getParentFile().mkdirs()){
                        getLog().info("Creation of " + schemaFile.getParentFile().getPath() + " failed");
                    }
                }
                SesameSchemaGen schemaGen = new SesameSchemaGen()
                    .setNamespace(prefix, namespace)
                    .setOntology(ontology)
                    .setOutputStream(new FileOutputStream(schemaFile))
                    .addExportNamespace(namespace);
                if (useTurtle){
                    schemaGen.generateTurtle(configuration);
                }else{
                    schemaGen.generateRDFXML(configuration);
                }        
            }            
            
            if (classListFile != null){
                StringBuilder builder = new StringBuilder();
                for (Class<?> clazz : entityClasses){
                    if (builder.length() > 0){
                        builder.append("\n");
                    }
                    builder.append(clazz.getName());
                }
                FileUtils.writeStringToFile(classListFile, builder.toString(), "UTF-8");
            }
            
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (OpenRDFException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }               
    }

    private List<Class<?>> getEntityClasses(URLClassLoader classLoader, ArchiveBrowser.Filter filter) throws IOException, 
        ClassNotFoundException {
        List<Class<?>> entityClasses = new ArrayList<Class<?>>();        
        for (URL url : classLoader.getURLs()){
            Iterator<?> classContents = ArchiveBrowser.getBrowser(url, filter);
            while (classContents.hasNext()){
                ClassFile classFile = null;
                DataInputStream in = null;
                try{
                    in = new DataInputStream((InputStream)classContents.next());
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
    
    protected URLClassLoader getProjectClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classpathElements = project.getCompileClasspathElements();
        List<URL> urls = new ArrayList<URL>(classpathElements.size());
        for (String element : classpathElements){
            File file = new File(element);
            if (file.exists()){
                urls.add(file.toURL());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());       
    }

    private String toRegex(String classes) {
        if (classes != null){
            return classes.replace("**", ".*").replace("*", "\\w+");
        }else{
            return "";
        }
    }

    

}
