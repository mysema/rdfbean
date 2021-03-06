/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MiniDialectTest {

    private MiniDialect dialect = new MiniDialect();

    @Test
    public void CreateBNode() {
        BID bid1 = dialect.createBNode();
        BID bid2 = dialect.createBNode();
        assertFalse(bid1.equals(bid2));
    }

    @Test
    public void CreateStatementIDUIDNODE() {
        STMT stmt = dialect.createStatement(RDFS.label, RDF.type, RDF.Property);
        assertEquals(RDFS.label, stmt.getSubject());
        assertEquals(RDF.type, stmt.getPredicate());
        assertEquals(RDF.Property, stmt.getObject());
    }

    @Test
    public void CreateStatementIDUIDNODEUID() {
        STMT stmt = dialect.createStatement(RDFS.label, RDF.type, RDF.Property, RDFS.label);
        assertEquals(RDFS.label, stmt.getSubject());
        assertEquals(RDF.type, stmt.getPredicate());
        assertEquals(RDF.Property, stmt.getObject());
        assertEquals(RDFS.label, stmt.getContext());
    }

    @Test
    public void GetNodeTypeNODE() {
        assertEquals(NodeType.URI, dialect.getNodeType(RDFS.label));
        assertEquals(NodeType.BLANK, dialect.getNodeType(new BID()));
        assertEquals(NodeType.LITERAL, dialect.getNodeType(new LIT("test")));
    }

    @Test
    public void GetObjectSTMT() {
        STMT stmt = dialect.createStatement(RDFS.label, RDF.type, RDF.Property, RDFS.label);
        assertEquals(RDF.Property, dialect.getObject(stmt));
    }

    @Test
    public void GetPredicateSTMT() {
        STMT stmt = dialect.createStatement(RDFS.label, RDF.type, RDF.Property, RDFS.label);
        assertEquals(RDF.type, dialect.getPredicate(stmt));
    }

    @Test
    public void GetSubjectSTMT() {
        STMT stmt = dialect.createStatement(RDFS.label, RDF.type, RDF.Property, RDFS.label);
        assertEquals(RDFS.label, dialect.getSubject(stmt));
    }

    @Test
    public void GetDatatypeUID() {
        assertNotNull(dialect.getDatatypeUID(XSD.stringType.getValue()));
    }

}
