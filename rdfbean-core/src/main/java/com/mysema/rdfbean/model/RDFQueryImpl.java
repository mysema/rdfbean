package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.support.QueryBase;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.ConstantImpl;
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
        super(new QueryMixin<RDFQueryImpl>(new DefaultQueryMetadata(false)));
        queryMixin.setSelf(this);
        this.connection = connection;
    }

    @Override
    public RDFQuery from(UID... graphs){
        for (UID uid : graphs){
            queryMixin.from(new ConstantImpl<UID>(UID.class, uid));
        }
        return this;
    }

    @Override
    public boolean ask(){
        return createBooleanQuery().getBoolean();
    }

    @Override
    public CloseableIterator<Map<String,NODE>> selectDistinct(Expression<?>... exprs){
        return distinct().select(exprs);
    }

    @Override
    public CloseableIterator<Map<String,NODE>> select(Expression<?>... exprs){
        return createTupleQuery(exprs).getTuples();
    }

    @Override
    public CloseableIterator<Map<String,NODE>> selectDistinctAll(){
        return distinct().selectAll();
    }

    @Override
    public CloseableIterator<Map<String,NODE>> selectAll(){
        return createTupleQuery().getTuples();
    }

    public Map<String, NODE> selectSingle(Expression<?>... exprs){
        CloseableIterator<Map<String,NODE>> it = select(exprs);
        try{
            if (it.hasNext()){
                return it.next();
            }else{
                return null;
            }
        }finally{
            it.close();
        }
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
        if (!blocks.isEmpty()){
            if (filters.getValue() == null){
                super.where(new GroupBlock(blocks));
            }else{
                super.where(new GroupBlock(blocks, filters.getValue()));
            }    
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

    public QueryMetadata getMetadata(){
        return queryMixin.getMetadata();
    }

    @Override
    public String toString(){
        QueryMetadata metadata = queryMixin.getMetadata().clone();
        if (filters.getValue() == null){
            metadata.addWhere(new GroupBlock(blocks));
        }else{
            metadata.addWhere(new GroupBlock(blocks, filters.getValue()));
        }   
        
        SPARQLVisitor visitor = new SPARQLVisitor();
        visitor.setInlineAll(true);
        visitor.visit(metadata, QueryLanguage.TUPLE);
        return visitor.toString();
    }
}
