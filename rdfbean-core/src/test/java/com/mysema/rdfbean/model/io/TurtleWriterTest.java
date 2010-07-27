/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.XSD;


public class TurtleWriterTest extends AbstractWriterTest{

    @Before
    public void setUp(){
        writer = new TurtleWriter(w);
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
        
        String expected = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns# > .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema# > .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema# > .\n\n" +
            
            "rdf:type rdf:type rdf:Property ; rdfs:label \"type\"^xsd:string , \"tyyppi\"@fi .\n\n" +
            
            "rdfs:Resource rdf:type rdfs:Class ; rdfs:label \"Resource\"^xsd:string .";
        assertEquals(expected, w.toString());
    }
    


}
