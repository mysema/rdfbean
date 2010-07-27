/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.XSD;


public class RDFXMLWriterTest extends AbstractWriterTest{

    @Before
    public void setUp(){
        writer = new RDFXMLWriter(w);
    }
    
    @Test
    public void test(){
        writer.start();
        writer.namespace("rdf", RDF.NS);
        writer.namespace("rdfs", RDFS.NS);
        writer.namespace("xsd", XSD.NS);
        writer.handle(new STMT(RDF.type, RDF.type, RDF.Property));
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("type")));
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));        
        writer.handle(new STMT(RDFS.Resource, RDF.type, RDFS.Class));
        writer.handle(new STMT(RDFS.Resource, RDFS.label, new LIT("Resource")));
        writer.end();
        
        System.out.println(w);
//        String expected = "";
//        assertEquals(expected, w.toString());
    }
    
}
