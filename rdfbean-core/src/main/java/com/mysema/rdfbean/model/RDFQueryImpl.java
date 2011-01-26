package com.mysema.rdfbean.model;

import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.support.QueryBase;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expression;

/**
 * @author tiwe
 *
 */
public class RDFQueryImpl extends QueryBase<RDFQueryImpl> implements RDFQuery {

    private final RDFConnection connection;
    
    public RDFQueryImpl(RDFConnection connection) {
        super(new QueryMixin<RDFQueryImpl>());
        queryMixin.setSelf(this);
        this.connection = connection;
    }
    
    @Override
    public boolean ask(){
        return createBooleanQuery().getBoolean();
    }
    
    @Override
    public CloseableIterator<Map<String,NODE>> select(Expression<?>... exprs){
        return createTupleQuery(exprs).getTuples();
    }

    @Override
    public CloseableIterator<STMT> construct(Block... exprs){
        return createGraphQuery(exprs).getTriples();
    }

    @Override
    public BooleanQuery createBooleanQuery(){
        return connection.createQuery(QueryLanguage.BOOLEAN, queryMixin.getMetadata());    
    }
    
    @Override
    public TupleQuery createTupleQuery(Expression<?>... exprs){
        queryMixin.addToProjection(exprs);
        return connection.createQuery(QueryLanguage.TUPLE, queryMixin.getMetadata());   
    }
    
    @Override
    public GraphQuery createGraphQuery(Block... exprs){
        queryMixin.addToProjection(exprs);
        return connection.createQuery(QueryLanguage.GRAPH, queryMixin.getMetadata());
    }
    
}
