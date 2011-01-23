package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.model.io.Format;
import com.mysema.rdfbean.owl.OWL;

public class ExportNamespacesTest extends AbstractRDBTest{
    
    @Before
    public void setUp() throws IOException{        
        RDFConnection connection = repository.openConnection();
        try{
            Set<STMT> added = new HashSet<STMT>();
            added.add(new STMT(RDF.type, RDF.type, RDF.Property));
            added.add(new STMT(RDF.Property, RDF.type, RDFS.Class));
            added.add(new STMT(OWL.Class, RDF.type, RDFS.Class));
            added.add(new STMT(new BID(), RDFS.label, new LIT("label", XSD.stringType)));
            connection.update(Collections.<STMT>emptySet(), added);            
        }finally{
            connection.close();
        }
    }
    
    @Test
    public void Default_Namespaces() throws UnsupportedEncodingException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.export(Format.TURTLE, null, baos);
        String result = new String(baos.toByteArray(), "UTF-8");
        assertTrue(result.contains("owl:Class a rdfs:Class"));
        assertTrue(result.contains("rdf:type a rdf:Property"));
        assertTrue(result.contains("rdf:Property a rdfs:Class"));
        assertTrue(result.contains("rdfs:label \"label\"^^xsd:string"));
    }
    
    @Test
    public void Explicit_Namespaces() throws UnsupportedEncodingException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String,String> ns2prefix = new HashMap<String,String>();
        ns2prefix.put(RDF.NS, "r");
        repository.export(Format.TURTLE, ns2prefix, null, baos);
        String result = new String(baos.toByteArray(), "UTF-8");
        System.err.println(result);
        assertTrue(result.contains("<http://www.w3.org/2002/07/owl#Class> a <http://www.w3.org/2000/01/rdf-schema#Class>"));
        assertTrue(result.contains("r:type a r:Property"));
        assertTrue(result.contains("r:Property a <http://www.w3.org/2000/01/rdf-schema#Class>"));
        assertTrue(result.contains("<http://www.w3.org/2000/01/rdf-schema#label> \"label\"^^<http://www.w3.org/2001/XMLSchema#string>"));
    }
    
}
