package com.mysema.rdfbean.jena;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.UID;

public class MemoryRepositoryTest {

    private DataSource dataSource;

    private MemoryRepository repository;

    @Before
    public void setUp() {
        dataSource = DataSourceImpl.createMem();
        repository = new MemoryRepository(dataSource);
    }

    @Test
    public void DefaultGraph_is_available() {
        assertNotNull(dataSource.getDefaultModel());
    }

    @Test
    public void NamedGraph_is_available() {
        repository.addGraph(new UID(TEST.NS));
        assertNotNull(dataSource.getNamedModel(TEST.NS));
        assertNotNull(dataSource.asDatasetGraph().getGraph(Node.createURI(TEST.NS)));
    }
}
