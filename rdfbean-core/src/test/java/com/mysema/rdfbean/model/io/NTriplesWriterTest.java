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


public class NTriplesWriterTest {
    
    StringWriter w = new StringWriter();
    RDFWriter writer = new NTriplesWriter(w);
    
    @Test
    public void test(){
        writer.start();
        writer.handle(new STMT(RDF.type, RDF.type, RDF.Property));
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("type")));
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("tyyppi", new Locale("fi"))));
        writer.end();
        
        String expected = "<"+RDF.type+"> <"+RDF.type+"> <"+RDF.Property+"> .\n" +
            "<"+RDF.type+"> <"+RDFS.label+"> " + new LIT("type") + " .\n" +
            "<"+RDF.type+"> <"+RDFS.label+"> " + new LIT("tyyppi", new Locale("fi")) +" .\n";
        assertEquals(expected, w.toString());
    }
    
    @Test
    public void BNodes_And_Literals(){
        UID subject = new UID(TEST.NS,"subject");
        UID predicate = new UID(TEST.NS,"predicate");
        BID bnode1 = new BID();
        BID bnode2 = new BID();
        
        writer.start();
        writer.handle(new STMT(bnode1, predicate, bnode2));
        writer.handle(new STMT(subject, predicate, bnode2));
        writer.handle(new STMT(subject, predicate, new LIT("str")));
        writer.handle(new STMT(subject, predicate, new LIT("str",Locale.CANADA_FRENCH)));
        writer.handle(new STMT(subject, predicate, new LIT("1",XSD.integerType)));
        writer.end();
        
        StringBuilder builder = new StringBuilder();
        builder.append("_:node1 <"       + predicate + "> _:node2 .\n");
        builder.append("<"+subject+"> <" + predicate + "> _:node2 .\n");
        builder.append("<"+subject+"> <" + predicate + "> "+new LIT("str")+" .\n");
        builder.append("<"+subject+"> <" + predicate + "> "+new LIT("str",Locale.CANADA_FRENCH)+" .\n");
        builder.append("<"+subject+"> <" + predicate + "> "+new LIT("1",XSD.integerType)+" .\n");
        
        assertEquals(builder.toString(), w.toString());
    }

    

}
