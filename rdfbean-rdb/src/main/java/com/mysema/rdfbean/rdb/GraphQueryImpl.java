package com.mysema.rdfbean.rdb;

import java.util.List;

import org.apache.commons.collections15.Transformer;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.Expression;
import com.mysema.rdfbean.model.GraphQuery;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.xsd.ConverterRegistry;

public class GraphQueryImpl implements GraphQuery{

    private final SQLQuery query;

    private final ConverterRegistry converters;

    private final PatternBlock pattern;

    private final List<Expression<?>> projection;

    private final Transformer<Long, NODE> transformer;

    public GraphQueryImpl(
            SQLQuery query,
            ConverterRegistry converters,
            PatternBlock pattern,
            List<Expression<?>> pr,
            Transformer<Long, NODE> transformer) {
        this.query = query;
        this.converters = converters;
        this.pattern = pattern;
        this.projection = pr;
        this.transformer = transformer;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        return query.iterate(new STMTFactoryExpression(pattern, projection, transformer));
    }

}
