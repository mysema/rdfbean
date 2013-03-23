package com.mysema.rdfbean.sparql;

import java.io.IOException;
import java.io.Writer;

import com.mysema.rdfbean.model.SPARQLQuery;

/**
 * @author tiwe
 * 
 */
public interface ResultProducer {

    /**
     * @param query
     * @param writer
     */
    void stream(SPARQLQuery query, Writer writer) throws IOException;

}
