package com.mysema.rdfbean.rdb;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class LiteralsTest extends AbstractRDBTest {

    private Set<STMT> added = new HashSet<STMT>();

    @Before
    public void setUp() throws IOException {
        RDFConnection connection = repository.openConnection();
        try {
            UID uri = new UID(TEST.NS, "test");
            added.add(new STMT(uri, RDFS.label, new LIT("1"), RDF.type));
            added.add(new STMT(uri, RDFS.label, new LIT("2", Locale.ENGLISH), RDF.type));
            added.add(new STMT(uri, RDFS.label, new LIT("3", XSD.stringType), RDF.type));
            added.add(new STMT(uri, RDFS.label, new LIT("4", XSD.intType), RDF.type));
            connection.update(Collections.<STMT> emptySet(), added);
        } finally {
            connection.close();
        }
    }

    @Test
    public void test() {
        RDFConnection connection = repository.openConnection();
        Set<STMT> stmts = new HashSet<STMT>(IteratorAdapter.asList(connection.findStatements(null, null, null, RDF.type, false)));
        assertEquals(added, stmts);
    }

}
