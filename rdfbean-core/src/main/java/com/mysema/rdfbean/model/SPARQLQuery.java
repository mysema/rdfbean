package com.mysema.rdfbean.model;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;

/**
 * SPARQLQuery defines the interface for accessing SPARQL query results
 * 
 * @author tiwe
 *
 */
public interface SPARQLQuery {
    
    public enum ResultType { TRIPLES, TUPLES }
    
    /**
     * Get the result type of the SPARQL query
     * 
     * @return
     */
    ResultType getResultType();
    
    /**
     * Get the result of a DESCRIBE or CONSTRUCT query as triples
     * 
     * @return
     */
    CloseableIterator<STMT> getTriples();
    
    /**
     * Get the result of a SELECT query as tuples
     * 
     * @return
     */
    CloseableIterator<Map<String,NODE>> getTuples();

    /**
     * Get the list of variables of a SELECT query
     * 
     * @return
     */
    List<String> getVariables();

    /**
     * Stream the results of a DESCRIBE or CONSTRUCT query
     * 
     * @param writer
     * @param contentType
     */
    void streamTriples(Writer writer, String contentType);

}
