package com.mysema.rdfbean.model;

import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public interface SPARQLQuery {
    
    public enum ResultType { TRIPLES, TUPLES }
    
    ResultType getResultType();
    
    CloseableIterator<STMT> getTriples();
    
    CloseableIterator<Map<String,NODE>> getTuples();

    List<String> getVariables();

}
