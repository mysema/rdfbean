/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

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


public class RDFXMLWriterTest {
    
    StringWriter w = new StringWriter();
    RDFWriter writer = new RDFXMLWriter(w);
    
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
        
        System.out.println(w);
    }
    
}
