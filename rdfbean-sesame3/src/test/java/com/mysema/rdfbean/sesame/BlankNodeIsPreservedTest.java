package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.mysema.rdfbean.model.BID;

public class BlankNodeIsPreservedTest {

    @Test
    public void IsPreserved() {
        ValueFactory valueFactory = new ValueFactoryImpl();
        SesameDialect dialect = new SesameDialect(valueFactory);
        BNode bNode = valueFactory.createBNode();
        BID bid = dialect.getBID(bNode);
        assertTrue(bNode == dialect.getBNode(bid));
    }

}
