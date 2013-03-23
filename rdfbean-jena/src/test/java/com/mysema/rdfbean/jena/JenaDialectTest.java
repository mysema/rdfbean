package com.mysema.rdfbean.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.XSD;

public class JenaDialectTest {

    private JenaDialect dialect = new JenaDialect();

    @Test
    public void LocalizedLiteral() {
        LIT lit = new LIT("X", Locale.ENGLISH);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("en", literal.getLiteralLanguage());
        assertNull(literal.getLiteralDatatype());
        assertEquals(lit, dialect.getLIT(literal));
    }

    @Test
    public void TypedLiteral() {
        LIT lit = new LIT("X", XSD.stringType);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("", literal.getLiteralLanguage());
        assertEquals(XSD.stringType.getId(), literal.getLiteralDatatype().getURI());
        assertEquals(lit, dialect.getLIT(literal));
    }

    @Test
    public void Literal() {
        LIT lit = new LIT("X", RDF.text);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("", literal.getLiteralLanguage());
        assertNull(literal.getLiteralDatatype());
        assertEquals(lit, dialect.getLIT(literal));
    }

    @Test
    public void Statement_in_default_Graph() {
        Node subject = Node.createURI(TEST.NS);
        Node predicate = Node.createURI(RDFS.label.getId());
        Node object = Node.createAnon();
        Quad quad = dialect.createStatement(subject, predicate, object);
        assertEquals(Quad.defaultGraphIRI, quad.getGraph());

    }

    @Test
    public void Statement_in_named_Graph() {
        Node subject = Node.createURI(TEST.NS);
        Node predicate = Node.createURI(RDFS.label.getId());
        Node object = Node.createAnon();
        Node context = subject;
        Quad quad = dialect.createStatement(subject, predicate, object, context);
        assertEquals(context, quad.getGraph());
    }

}
