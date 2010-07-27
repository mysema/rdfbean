/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Locale;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


public class TurtleWriterTest {

    StringWriter w = new StringWriter();
    RDFWriter writer = new TurtleWriter(w);    
    
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
    
    @Test
    public void BNodes_And_Literals(){
        UID subject = new UID(TEST.NS,"subject");
        UID predicate = new UID(TEST.NS,"predicate");
        BID bnode1 = new BID();
        BID bnode2 = new BID();
        
        writer.start();
        writer.namespace("rdf", RDF.NS);
        writer.namespace("rdfs", RDFS.NS);
        writer.namespace("xsd", XSD.NS);
        writer.namespace("test", TEST.NS);
        writer.handle(new STMT(bnode1, predicate, bnode2));
        writer.handle(new STMT(subject, predicate, bnode2));
        writer.handle(new STMT(subject, predicate, new LIT("str")));
        writer.handle(new STMT(subject, predicate, new LIT("str",Locale.CANADA_FRENCH)));
        writer.handle(new STMT(subject, predicate, new LIT("1",XSD.integerType)));
        writer.end();
        
        String expected = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns# > .\n" +
             "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema# > .\n" +
             "@prefix xsd: <http://www.w3.org/2001/XMLSchema# > .\n" +
             "@prefix test: <http://semantics.mysema.com/test# > .\n\n" +

             "_:node1 test:predicate _:node2 .\n\n" +

             "test:subject test:predicate _:node2 , \"str\"^xsd:string , \"str\"@fr-ca , \"1\"^xsd:integer .";
        assertEquals(expected, w.toString());
    }


}
