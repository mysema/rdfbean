package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.EntityDomain;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;


public class SesameSchemaGenTest {

    @Test
    public void Generate() throws StoreException, RDFHandlerException, RDFParseException, IOException{
        Configuration configuration = new DefaultConfiguration(EntityDomain.Entity.class);
        File schemaFile = new File("target/schema.ttl");

        SesameSchemaGen schemaGen = new SesameSchemaGen();
        schemaGen.addExportNamespace(TEST.NS);
        schemaGen.setConfiguration(configuration);
        schemaGen.setOutputStream(new FileOutputStream(schemaFile));
        schemaGen.generateTurtle(configuration);

        assertTrue(new File("target/schema.ttl").exists());
    }

}
