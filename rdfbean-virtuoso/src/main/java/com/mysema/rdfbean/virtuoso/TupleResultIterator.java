package com.mysema.rdfbean.virtuoso;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.NODE;

/**
 * @author tiwe
 *
 */
public class TupleResultIterator implements CloseableIterator<Map<String, NODE>>{

    private final Statement stmt;
    
    private final ResultSet rs;
    
    private final Converter converter;
    
    private final Map<String, NODE> bindings;
    
    private final List<String> variables;
    
    @Nullable
    private Boolean next;
    
    public TupleResultIterator(Statement stmt, ResultSet rs, Converter converter, List<String> variables, Map<String, NODE> bindings) {
        this.stmt = stmt;
        this.rs = rs;
        this.converter = converter;
        this.variables = variables;
        this.bindings = bindings;
    }

    @Override
    public void close() {
        AbstractQueryImpl.close(stmt, rs);
    }

    @Override
    public boolean hasNext() {
        if (next == null){
            try {
                next = rs.next();
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }
        return next;
    }

    @Override
    public Map<String, NODE> next() {
        if (hasNext()){
            try {
                next = null;
                Map<String,NODE> tuples = new HashMap<String,NODE>();
                for (String variable : variables){
                    Object obj = rs.getObject(variable);
                    if (obj != null){
                        tuples.put(variable, converter.toNODE(obj));    
                    }else{
                        tuples.put(variable, bindings.get(variable));
                    }                    
                }
                return tuples;
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }else{
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();        
    }

}
