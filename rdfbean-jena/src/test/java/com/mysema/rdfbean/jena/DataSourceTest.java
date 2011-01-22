package com.mysema.rdfbean.jena;

import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.RDFS;

public class DataSourceTest {

    @Test
    @Ignore
    public void NamedGraph_Usage(){
        DataSource dataSource = DataSourceImpl.createMem();
        Node subject = Node.createURI(TEST.NS);
        Node predicate = Node.createURI(RDFS.label.getId());
        Node object = Node.createAnon();
        dataSource.asDatasetGraph().add(new Quad(Quad.defaultGraphIRI, subject, predicate, object));        
        dataSource.asDatasetGraph().deleteAny(null, subject, null, null);
    }
}
