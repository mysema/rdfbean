package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RDFQueryTest {

    @Test
    public void ToString() {
        MiniConnection connection = new MiniRepository().openConnection();
        RDFQuery query = new RDFQueryImpl(connection);
        query.where(QNODE.s.a(RDFS.Resource));
        assertEquals(
                "SELECT * WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Resource> } ",
                query.toString().replaceAll("\\s+", " "));
    }

    @Test
    public void ToString_After_Invocation() {
        MiniConnection connection = new MiniRepository().openConnection();
        RDFQuery query = new RDFQueryImpl(connection);
        query.where(QNODE.s.a(RDFS.Resource));
        query.createBooleanQuery();
        assertEquals(
                "SELECT * WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Resource> } ",
                query.toString().replaceAll("\\s+", " "));
    }

}
