package com.mysema.rdfbean.model;

import java.io.Writer;

/**
 * SPARQLQuery defines the interface for accessing SPARQL query results
 *
 * @author tiwe
 *
 */
public interface SPARQLQuery extends BooleanQuery, GraphQuery, TupleQuery {

    public enum ResultType { BOOLEAN, TRIPLES, TUPLES }

    /**
     * Get the result type of the SPARQL query
     *
     * @return
     */
    ResultType getResultType();

    /**
     * Stream the results of a DESCRIBE or CONSTRUCT query
     *
     * @param writer
     * @param contentType
     */
    void streamTriples(Writer writer, String contentType);

    /**
     * Add the given binding
     *
     * @param variable
     * @param node
     */
    void setBinding(String variable, NODE node);

    /**
     * Set the maximum query time in seconds
     *
     * @param secs
     */
    void setMaxQueryTime(int secs);

}
