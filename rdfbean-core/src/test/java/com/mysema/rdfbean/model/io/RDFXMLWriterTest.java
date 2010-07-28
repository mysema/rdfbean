/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model.io;

import static org.junit.Assert.*;

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
        
        assertEquals(
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" " +
            "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
            "<rdf:Description rdf:about=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\">" +
            "<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\"/>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\">" +
            "<rdfs:label>type</rdfs:label>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\">" +
            "<rdfs:label xml:lang=\"fi\">tyyppi</rdfs:label>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://www.w3.org/2000/01/rdf-schema#Resource\">" +
            "<rdf:type rdf:resource=\"http://www.w3.org/2000/01/rdf-schema#Class\"/>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://www.w3.org/2000/01/rdf-schema#Resource\">" +
            "<rdfs:label>Resource</rdfs:label>" +
            "</rdf:Description>" +
            "</rdf:RDF>", w.toString());
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
        
        assertEquals(
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
            "xmlns:test=\"http://semantics.mysema.com/test#\" " +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" " +
            "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
            "<rdf:Description rdf:nodeID=\"node1\">" +
            "<test:predicate rdf:nodeID=\"node2\"/>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://semantics.mysema.com/test#subject\">" +
            "<test:predicate rdf:nodeID=\"node2\"/>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://semantics.mysema.com/test#subject\">" +
            "<test:predicate>str</test:predicate>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://semantics.mysema.com/test#subject\">" +
            "<test:predicate xml:lang=\"fr-ca\">str</test:predicate>" +
            "</rdf:Description>" +
            "<rdf:Description rdf:about=\"http://semantics.mysema.com/test#subject\">" +
            "<test:predicate rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">1</test:predicate>" +
            "</rdf:Description>" +
            "</rdf:RDF>",
            w.toString());
            
    }
    
}
