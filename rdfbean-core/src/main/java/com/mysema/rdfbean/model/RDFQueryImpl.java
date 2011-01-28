package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.support.QueryBase;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;

/**
 * @author tiwe
 *
 */
public class RDFQueryImpl extends QueryBase<RDFQueryImpl> implements RDFQuery {

    private final RDFConnection connection;
    
    private List<Block> blocks = new ArrayList<Block>();
    
    private BooleanBuilder filters = new BooleanBuilder();
    
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
        aggregateFilters();
        return connection.createQuery(QueryLanguage.BOOLEAN, queryMixin.getMetadata());    
    }
    
    @Override
    public TupleQuery createTupleQuery(Expression<?>... exprs){
        aggregateFilters();
        queryMixin.addToProjection(exprs);
        return connection.createQuery(QueryLanguage.TUPLE, queryMixin.getMetadata());   
    }
    
    @Override
    public GraphQuery createGraphQuery(Block... exprs){
        aggregateFilters();
        queryMixin.addToProjection(exprs);
        return connection.createQuery(QueryLanguage.GRAPH, queryMixin.getMetadata());
    }
    
    protected void aggregateFilters(){
        if (filters.getValue() == null){
            super.where(new GroupBlock(blocks));
        }else{
            super.where(new GroupBlock(blocks, filters.getValue()));
        }
        blocks = new ArrayList<Block>();
        filters = new BooleanBuilder();
    }
    
    @Override
    public RDFQueryImpl where(Predicate... o) {
        for (Predicate predicate : o){
            if (predicate instanceof Block){
                blocks.add((Block)predicate);
            }else{
                filters.and(predicate);
            }
        }
        return this;
    }
    
    @Override
    public String toString(){
        SPARQLVisitor visitor = new SPARQLVisitor();
        visitor.visit(queryMixin.getMetadata(), QueryLanguage.TUPLE);
        return visitor.toString();
    }
}
