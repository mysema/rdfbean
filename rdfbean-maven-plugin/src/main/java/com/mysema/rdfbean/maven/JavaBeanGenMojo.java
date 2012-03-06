/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.mysema.rdfbean.beangen.JavaBeanExporter;
import com.mysema.rdfbean.model.Format;
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

    /**
     * @parameter
     */
    private Properties nsToPackage;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MemoryRepository repository = new MemoryRepository();
        Format format = useTurtle ? Format.TURTLE : Format.RDFXML;
        try {
            repository.setSources(new RDFSource(schemaFile.getAbsoluteFile().toURI().toURL().toString(), format, "http://www.mysema.com"));
        } catch (MalformedURLException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        }

        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.addPackages(RDFSClass.class.getPackage(), OWLClass.class.getPackage());

        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(configuration);
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        Session session = sessionFactory.openSession();
        try{
            JavaBeanExporter exporter = new JavaBeanExporter(true);
            for (Map.Entry<Object, Object> entry : nsToPackage.entrySet()){
                exporter.addPackage(entry.getKey().toString(), entry.getValue().toString());
            }
            exporter.export(session, targetFolder);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }finally{
            try {
                session.close();
            }finally{
                sessionFactory.close();
            }
        }
    }

}
