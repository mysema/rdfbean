package com.mysema.rdfbean.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.Query;
import com.mysema.query.types.Expression;

/**
 * @author tiwe
 *
 */
public interface RDFQuery extends Query<RDFQuery>{

    boolean ask();

    CloseableIterator<Map<String, NODE>> select(Expression<?>... exprs);

    @Nullable
    Map<String, NODE> selectSingle(Expression<?>... exprs);

    CloseableIterator<STMT> construct(Block... exprs);

    BooleanQuery createBooleanQuery();

    TupleQuery createTupleQuery(Expression<?>... exprs);

    GraphQuery createGraphQuery(Block... exprs);

}