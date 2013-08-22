/*
 * Copyright (c) 2009 Mysema Ltd
 * All rights reserved.
 *
 */
package com.mysema.rdf.demo;

import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Format;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.schema.SchemaGen;

/**
 * BMSchemaGen provides
 * 
 * @author tiwe
 * @version $Id$
 */
public class DemoSchemaGen {

    public static void main(String[] args) throws IOException {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:/persistence.xml");
        DemoSchemaGen schemaGen = new DemoSchemaGen((Configuration) appContext.getBean("configuration"));
        schemaGen.generateBMSchema();
    }

    private final Configuration configuration;


    public DemoSchemaGen(Configuration configuration) {
        this.configuration = Assert.notNull(configuration, "configuration");
    }


    public void generateBMSchema() throws IOException {
        new SchemaGen().setNamespace("demo", DEMO.NS).setOntology(DEMO.CONTEXT).addExportNamespace(DEMO.NS).setConfiguration(configuration)
                .export(Format.RDFXML, new FileOutputStream("src/main/resources/demo.owl"));
    }

}
