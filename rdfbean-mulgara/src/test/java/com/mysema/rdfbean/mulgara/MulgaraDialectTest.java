package com.mysema.rdfbean.mulgara;

import static org.junit.Assert.assertNotNull;

import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Node;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;
import org.junit.Before;
import org.junit.Test;
import org.mulgara.client.jrdf.GraphElementBuilder;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;


public class MulgaraDialectTest {
    
    private MulgaraDialect dialect;
    
    @Before
    public void setUp() throws GraphException{
        GraphElementFactory factory = new GraphElementBuilder();
        dialect = new MulgaraDialect(factory);        
    }

    @Test
    public void createBNode(){
        assertNotNull(dialect.createBNode());
    }
    
    @Test
    public void createStatement(){                
        SubjectNode subject = dialect.getURI(RDF.type);
        URIReference predicate = dialect.getURI(RDF.type);
        Node object = dialect.getURI(RDF.Property);
        assertNotNull(dialect.createStatement(subject, predicate, object));
        assertNotNull(dialect.createStatement(subject, predicate, object, dialect.getURI(new UID(TEST.NS,"context"))));
    }
    
    @Test
    public void getDatatypeURI(){
        assertNotNull(dialect.getDatatypeURI(XSD.stringType.getId()));
    }
    
    @Test
    public void getNode(){
        URIReference rdfType = dialect.getURI(RDF.type);
        assertNotNull(dialect.getID(rdfType));
        assertNotNull(dialect.getUID(rdfType));
        assertNotNull(dialect.getLIT(dialect.getLiteral(new LIT(""))));
        assertNotNull(dialect.getBID(dialect.getBNode(new BID("123"))));
        assertNotNull(dialect.getID(dialect.getBNode(new BID("123"))));
    }

}
