package com.mysema.rdfbean.sesame;

import java.util.List;
import java.util.Map;

import org.openrdf.query.GraphQuery;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class GraphQueryImpl implements SPARQLQuery{
    
    private final GraphQuery graphQuery;
    
    private final SesameDialect dialect;
    
    public GraphQueryImpl(GraphQuery graphQuery, SesameDialect dialect) {
        this.graphQuery = graphQuery;
        this.dialect = dialect;
    }
    
    @Override
    public ResultType getResultType() {
        return ResultType.TRIPLES;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        try {
            return new GraphResultIterator(graphQuery.evaluate(), dialect);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }                
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getVariables() {
        throw new UnsupportedOperationException();
    }


}
