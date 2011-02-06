package com.mysema.rdfbean.rdb;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

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
    
    private final Transformer<Long, NODE> transformer;
    
    public TupleQueryImpl(
            SQLQuery query, 
            ConverterRegistry converters,
            List<String> variables, 
            List<Expression<?>> pr, 
            Transformer<Long, NODE> transformer) {
        this.query = query;
        this.converters = converters;
        this.variables = variables;
        this.projection = pr;
        this.transformer = transformer;
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        return query.iterate(new TupleFactoryExpression(converters, variables, projection, transformer));
    }

    @Override
    public List<String> getVariables() {
        return variables;
    }

}
