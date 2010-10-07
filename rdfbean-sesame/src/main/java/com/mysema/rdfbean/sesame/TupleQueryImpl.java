package com.mysema.rdfbean.sesame;

import java.util.Map;
import java.util.Set;

import org.openrdf.query.TupleQuery;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

public class TupleQueryImpl implements SPARQLQuery {
    
    private final TupleQuery query;
    
    private final SesameDialect dialect;

    public TupleQueryImpl(TupleQuery query, SesameDialect dialect) {
        this.query = query;
        this.dialect = dialect;
    }

    @Override
    public ResultType getResultType() {
        return ResultType.TUPLES;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        try {
            return new TupleResultIterator(query.evaluate(), dialect);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Set<String> getVariables() {
        return query.getBindings().getBindingNames();
    }

}
