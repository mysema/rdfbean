package com.mysema.rdfbean.model;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public interface GraphQuery {
    
    /**
     * Get the result of a DESCRIBE or CONSTRUCT query as triples
     *
     * @return
     */
    CloseableIterator<STMT> getTriples();

}
