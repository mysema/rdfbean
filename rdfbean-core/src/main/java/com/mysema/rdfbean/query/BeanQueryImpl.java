/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableInt;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.QueryException;
import com.mysema.query.QueryMetadata;
import com.mysema.query.SearchResults;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.rdfbean.model.BooleanQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 */
public class BeanQueryImpl extends ProjectableQuery<BeanQueryImpl> implements
        BeanQuery, Closeable {

    private final Session session;

    private final Ontology ontology;

    private final ConverterRegistry converterRegistry;

    private final RDFConnection connection;

    public BeanQueryImpl(Session session, Ontology ontology, RDFConnection connection) {
        super(new QueryMixin<BeanQueryImpl>());
        queryMixin.setSelf(this);
        this.session = session;
        this.ontology = ontology;
        this.converterRegistry = session.getConfiguration().getConverterRegistry();
        this.connection = connection;
    }

    @Override
    public void close() throws IOException {
        // ?!?
    }

    @Override
    public long count() {
        TupleQuery query = createTupleQuery(true);
        if (!connection.getQueryOptions().isCountViaAggregation()){
            long counter = 0;
            CloseableIterator<Map<String,NODE>> tuples = query.getTuples();
            try{
                while (tuples.hasNext()){
                    counter++;
                    tuples.next();
                }
            }finally{
                tuples.close();
            }
            return counter;

        }else{
            List<Map<String, NODE>> results = IteratorAdapter.asList(query.getTuples());
            NODE result = results.get(0).values().iterator().next();
            if (result.isLiteral()) {
                return Long.valueOf(result.getValue());
            } else {
                throw new IllegalArgumentException(result.toString());
            }
        }
    }

    private RDFQueryBuilder createBuilder(){
        return new RDFQueryBuilder(
                connection,
                session,
                session.getConfiguration(),
                ontology,
                queryMixin.getMetadata());
    }

    private BooleanQuery createBooleanQuery() {
        return createBuilder().createBooleanQuery();
    }

    private TupleQuery createTupleQuery(boolean forCount) {
        return createBuilder().createTupleQuery(forCount);
    }

    @Override
    public boolean exists() {
        return createBooleanQuery().getBoolean();
    }

    @Override
    public BeanQuery from(EntityPath<?>... o) {
        return queryMixin.from(o);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <RT> RT getAsProjectionValue(Expression<RT> expr,
            Map<String, NODE> nodes, List<String> variables, MutableInt offset) {
        if (expr instanceof FactoryExpression<?>) {
            FactoryExpression<?> factoryExpr = (FactoryExpression<?>) expr;
            Object[] args = new Object[factoryExpr.getArgs().size()];
            for (int i = 0; i < args.length; i++) {
                NODE node = nodes.get(variables.get(offset.intValue() + i));
                args[i] = getAsProjectionValue(node, factoryExpr.getArgs().get(i).getType());
            }
            offset.add(args.length);
            try {
                return (RT) factoryExpr.newInstance(args);
            } catch (Exception e) {
                throw new QueryException(e.getMessage(), e);
            }
        } else {
            NODE node = nodes.get(variables.get(offset.intValue()));
            offset.add(1);
            if (node != null) {
                return getAsProjectionValue(node, expr.getType());
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <RT> RT getAsProjectionValue(NODE node, Class<RT> type) {
        if (node.isResource()) {
            if (type.equals(String.class)) {
                // TODO : always return LID ?
                return (RT) session.getLID(node.asResource()).getId();
            } else {
                return session.get(type, node.asResource());
            }
        } else {
            return converterRegistry.fromString(node.getValue(), type);
        }
    }

    @Override
    public CloseableIterator<Object[]> iterate(final Expression<?>[] args) {
        queryMixin.addToProjection(args);
        final TupleQuery query = createTupleQuery(false);
        final CloseableIterator<Map<String, NODE>> results = query.getTuples();
        return new CloseableIterator<Object[]>() {
            @Override
            public void close() {
                results.close();
            }

            @Override
            public boolean hasNext() {
                return results.hasNext();
            }

            @Override
            public Object[] next() {
                Map<String, NODE> row = results.next();
                Object[] rv = new Object[args.length];
                MutableInt offset = new MutableInt();
                for (int i = 0; i < rv.length; i++) {
                    rv[i] = getAsProjectionValue(args[i], row, query.getVariables(), offset);
                }
                return rv;
            }

            @Override
            public void remove() {
                results.remove();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> List<RT> list(Expression<RT> projection){
        if (!converterRegistry.supports(projection.getType())){
            // bulk load of resources
            queryMixin.addToProjection(projection);
            TupleQuery query = createTupleQuery(false);    
            CloseableIterator<Map<String, NODE>> results = query.getTuples();
            List<ID> ids = new ArrayList<ID>();
            try{
                while (results.hasNext()){
                    Map<String, NODE> row = results.next();
                    if (!row.isEmpty()){
                        ids.add(row.values().iterator().next().asResource());    
                    }else{
                        ids.add(null);
                    }                    
                }
            }finally{
                results.close();
            }
            return (List)session.getAll(projection.getType(), ids.toArray(new ID[ids.size()]));            
        }else{
            return super.list(projection);
        }        
    }
    
    @Override
    public <RT> CloseableIterator<RT> iterate(final Expression<RT> projection) {
        queryMixin.addToProjection(projection);
        final TupleQuery query = createTupleQuery(false);
        final CloseableIterator<Map<String, NODE>> results = query.getTuples();
        return new CloseableIterator<RT>() {
            @Override
            public void close() {
                results.close();
            }

            @Override
            public boolean hasNext() {
                return results.hasNext();
            }

            @Override
            public RT next() {
                Map<String, NODE> row = results.next();
                return getAsProjectionValue(projection, row, query
                        .getVariables(), new MutableInt());
            }

            @Override
            public void remove() {
                results.remove();
            }
        };
    }

    @Override
    public <RT> SearchResults<RT> listResults(Expression<RT> projection) {
        queryMixin.addToProjection(projection);
        long total = count();

        QueryMetadata md = queryMixin.getMetadata();
        md.clearProjection();
        List<RT> results = list(projection);
        return new SearchResults<RT>(results,
                md.getModifiers().getLimit(),
                md.getModifiers().getOffset(),
                total);
    }
}
