/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mysema.rdfbean.beangen.JavaBeanExporter;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.model.io.RDFSource;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.sesame.MemoryRepository;

/**
 * JavaBeanGenMojo provides a Maven plugin for Java Bean generation from OWL/RDF-S schemas
 *
 * @author tiwe
 * @version $Id$
 * 
 * @goal beangen
 * @phase generate-sources
 */
public class JavaBeanGenMojo extends AbstractMojo{

    /** 
     * @parameter  
     */
    private File schemaFile;
    
    /**
     * @parameter
     */
    private File targetFolder;
    
    /** 
     * @parameter 
     */
    private boolean useTurtle;
    
    // TODO : ns to package mappings
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MemoryRepository repository = new MemoryRepository();
        Format format = useTurtle ? Format.TURTLE : Format.RDFXML;
        repository.setSources(new RDFSource(schemaFile.getPath(), format, ""));
        
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.addPackages(RDFSClass.class.getPackage(), OWLClass.class.getPackage());
        
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        Session session = sessionFactory.openSession();
        try{
            JavaBeanExporter exporter = new JavaBeanExporter(true);
            exporter.export(session, targetFolder);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }finally{
            try {
                session.close();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }finally{
                sessionFactory.close();
            }
        }
    }

}
