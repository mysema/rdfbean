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


public class NTriplesWriterTest extends AbstractWriterTest{
    
    @Before
    public void setUp(){
        writer = new NTriplesWriter(w);
    }
    
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
    

}
