package com.mysema.rdfbean.model.io;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class TurtleStringWriterTest {
    
    private final TurtleStringWriter writer = new TurtleStringWriter();

    @Test
    public void Handle(){
        List<UID> predicates = new ArrayList<UID>(10);
        for (int i = 0; i < 10; i++){
            predicates.add(new UID(TEST.NS, "pred"+i));
        }
        
                             
        List<STMT> stmts = new ArrayList<STMT>(140);
        for (int i = 0; i < 14; i++){
            ID sub = new UID(TEST.NS, "e" + UUID.randomUUID());  
            stmts.add(new STMT(sub, predicates.get(0), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(1), new LIT("1", XSD.intType)));
            stmts.add(new STMT(sub, predicates.get(2), sub));
            stmts.add(new STMT(sub, predicates.get(3), new BID()));
            stmts.add(new STMT(sub, predicates.get(4), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(5), new LIT("2", XSD.intType)));
            stmts.add(new STMT(sub, predicates.get(6), sub));
            stmts.add(new STMT(sub, predicates.get(7), new BID()));
            stmts.add(new STMT(sub, predicates.get(8), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(9), new LIT("3", XSD.intType)));
        }
        
        writer.begin();
        for (STMT stmt : stmts){
            writer.handle(stmt);
        }
        writer.end();
        
        System.out.println(writer.toString());
        String str = writer.toString();
        assertTrue(str.contains(" ; ns1:pred1"));
        assertTrue(str.contains("@prefix ns1: <http://semantics.mysema.com/test#> ."));
        assertTrue(str.contains("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."));
        assertTrue(str.contains("\"3\"^^xsd:int"));
    }
    
    @Test
    public void Duplicate_Predicate(){    
        writer.handle(new STMT(RDF.type, RDF.type, RDF.Property));
        writer.handle(new STMT(RDF.type, RDF.type, RDFS.Resource));
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("X")));
        writer.end();
        
        System.out.println(writer.toString());
        String str = writer.toString();
        assertTrue(str.contains("rdf:type a rdf:Property , rdfs:Resource ; rdfs:label \"X\"^^xsd:string ."));
    }
    
    @Test
    public void Duplicate_Predicate2(){
        writer.handle(new STMT(RDF.type, RDFS.label, new LIT("X")));
        writer.handle(new STMT(RDF.type, RDF.type, RDF.Property));
        writer.handle(new STMT(RDF.type, RDF.type, RDFS.Resource));        
        writer.end();
        
        System.out.println(writer.toString());
        String str = writer.toString();
        assertTrue(str.contains("rdf:type rdfs:label \"X\"^^xsd:string ; a rdf:Property , rdfs:Resource ."));
    }
    
    @Test
    public void Proper_Name_Usage(){
        UID subject = new UID("http://dbpedia.org/resource/Torpparinm%C3%A4ki");
        writer.begin();
        writer.handle(new STMT(subject, RDF.type, RDFS.Resource));
        writer.end();
        
        System.out.println(writer.toString());
        String str = writer.toString();
        assertTrue(str.contains("<http://dbpedia.org/resource/Torpparinm%C3%A4ki> a rdfs:Resource ."));
    }
}
