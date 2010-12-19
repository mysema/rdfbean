package com.mysema.rdfbean.model.io;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.DC;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public abstract class AbstractWriterTest {

    private StringWriter s = new StringWriter();
    
    private RDFWriter writer = createWriter(s);
    
    @Test
    public void test(){
        writer.begin();
        UID uid = new UID("http://www.w3.org/2001/08/rdf-test/");
        BID bid = new BID("a");
        writer.handle(new STMT(uid, DC.creator, new LIT("Dave Beckett")));
        writer.handle(new STMT(uid, DC.creator, new LIT("Jan Grant")));
        writer.handle(new STMT(uid, DC.publisher, bid));
        writer.handle(new STMT(bid, DC.title,   new LIT("World Wide Web Consortium", Locale.ENGLISH)));
        writer.handle(new STMT(bid, DC.source,  new UID("http://www.w3.org/")));
        uid = new UID(TEST.NS,"thing");
        writer.handle(new STMT(uid, RDF.type,   RDFS.Resource));
        writer.handle(new STMT(uid, RDFS.label, new LIT("\t")));
        writer.handle(new STMT(uid, RDFS.label, new LIT("&\n&")));
        writer.end();
        System.out.println(s);
        validate(s.toString());
    }

    protected abstract RDFWriter createWriter(Writer w);
    
    protected abstract void validate(String str);
    
}
