package com.mysema.rdfbean.rdb;

import java.util.List;

import com.google.common.base.Function;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.Expression;
import com.mysema.rdfbean.model.GraphQuery;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.STMT;

public class GraphQueryImpl implements GraphQuery{

    private final SQLQuery query;

    private final PatternBlock pattern;

    private final List<Expression<?>> projection;

    private final Function<Long, NODE> function;

    public GraphQueryImpl(
            SQLQuery query,
            PatternBlock pattern,
            List<Expression<?>> pr,
            Function<Long, NODE> function) {
        this.query = query;
        this.pattern = pattern;
        this.projection = pr;
        this.function = function;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        return query.iterate(new STMTFactoryExpression(pattern, projection, function));
    }

}
