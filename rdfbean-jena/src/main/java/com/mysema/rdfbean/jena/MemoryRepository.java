package com.mysema.rdfbean.jena;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DataSourceImpl;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public class MemoryRepository extends JenaRepository{

    public MemoryRepository() {
        this(DataSourceImpl.createMem());
    }
    
    public MemoryRepository(Dataset dataset) {
        super(dataset);
    }
    
    public void addGraph(UID context){
        addGraph(context, Factory.createGraphMem());
    }
    
    public void addGraph(UID context, Graph g){
        graph.addGraph(Node.createURI(context.getId()), g);
    }

}
