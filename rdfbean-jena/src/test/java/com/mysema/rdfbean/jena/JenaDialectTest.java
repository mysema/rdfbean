package com.mysema.rdfbean.jena;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.XSD;


public class JenaDialectTest {
    
    private JenaDialect dialect = new JenaDialect();
    
    @Test
    public void LocalizedLiteral(){
        LIT lit = new LIT("X", Locale.ENGLISH);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("en",literal.getLiteralLanguage());
        assertNull(literal.getLiteralDatatype());
        assertEquals(lit, dialect.getLIT(literal));
    }
    
    @Test
    public void TypedLiteral(){
        LIT lit = new LIT("X", XSD.stringType);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("", literal.getLiteralLanguage());
        assertEquals(XSD.stringType.getId(), literal.getLiteralDatatype().getURI());
        assertEquals(lit, dialect.getLIT(literal));
    }
    
    @Test
    public void Literal(){
        LIT lit = new LIT("X", RDF.text);
        Node literal = dialect.getLiteral(lit);
        assertTrue(literal.isLiteral());
        assertEquals("", literal.getLiteralLanguage());
        assertNull(literal.getLiteralDatatype());
        assertEquals(lit, dialect.getLIT(literal));
    }

}
