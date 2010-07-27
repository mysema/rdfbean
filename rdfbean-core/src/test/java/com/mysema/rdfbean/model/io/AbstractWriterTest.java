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

public abstract class AbstractWriterTest {
    
    StringWriter w = new StringWriter();
    RDFWriter writer;

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
