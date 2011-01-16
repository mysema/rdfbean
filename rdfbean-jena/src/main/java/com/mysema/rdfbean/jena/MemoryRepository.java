package com.mysema.rdfbean.jena;

import com.hp.hpl.jena.graph.Factory;

/**
 * @author tiwe
 *
 */
public class MemoryRepository extends JenaRepository{

    public MemoryRepository() {
        super(Factory.createGraphMem());
    }

}
