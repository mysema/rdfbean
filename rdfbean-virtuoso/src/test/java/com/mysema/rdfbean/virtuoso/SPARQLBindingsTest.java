package com.mysema.rdfbean.virtuoso;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;

public class SPARQLBindingsTest {

    private final Map<String,NODE> bindings = new HashMap<String,NODE>();
    
    private final List<NODE> nodes = new ArrayList<NODE>();
    
    @Test
    public void Resource_Binding(){
        bindings.put("p", RDF.type);
        String query = "select ?s ?p ?o where { ?s ?p ?o }";
        assertEquals("select ?s ?p ?o where { ?s `iri(??)` ?o }", normalize(query));
    }
    
    @Test
    public void Literal_Binding(){
        bindings.put("o", new LIT("x"));
        String query = "select ?s ?p ?o where { ?s ?p ?o }";
        assertEquals("select ?s ?p ?o where { ?s ?p `bif:__rdf_long_from_batch_params(??,??,??)` }", normalize(query));
    }

    private String normalize(String query) {
        return AbstractQueryImpl.normalize(query, bindings, nodes);
    }
    
}
