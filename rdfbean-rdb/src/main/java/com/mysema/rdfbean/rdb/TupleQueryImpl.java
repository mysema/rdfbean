package com.mysema.rdfbean.rdb;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.Expression;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.xsd.ConverterRegistry;

public class TupleQueryImpl implements TupleQuery{
    
    private final SQLQuery query;
    
    private final ConverterRegistry converters;
    
    private final List<String> variables;
    
    private final List<Expression<?>> projection;
    
    private final Function<Long, NODE> function;
    
    public TupleQueryImpl(
            SQLQuery query, 
            ConverterRegistry converters,
            List<String> variables, 
            List<Expression<?>> pr, 
            Function<Long, NODE> function) {
        this.query = query;
        this.converters = converters;
        this.variables = variables;
        this.projection = pr;
        this.function = function;
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        return query.iterate(new TupleFactoryExpression(converters, variables, projection, function));
    }

    @Override
    public List<String> getVariables() {
        return variables;
    }

}
