package com.mysema.rdfbean.model;

import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public interface TupleQuery {

    /**
     * Get the result of this query as tuples
     *
     * @return
     */
    CloseableIterator<Map<String,NODE>> getTuples();
    
    /**
     * Get the list of variables of the query
     *
     * @return
     */
    List<String> getVariables();

}
