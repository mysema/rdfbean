package com.mysema.rdfbean.virtuoso;

import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.QueryException;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class TupleQueryImpl extends AbstractQueryImpl {
    
    private final Converter converter;
    
    @Nullable
    private List<String> variables = null;
    
    @Nullable
    private CloseableIterator<Map<String, NODE>> tuples = null;
    
    public TupleQueryImpl(Connection connection, Converter converter, int prefetch, String query) {
        super(connection, prefetch, query);
        this.converter = converter;
    }

    @Override
    public boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultType getResultType() {
        return ResultType.TUPLES;        
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        if (tuples == null){
            try{
                produceResults();
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }
        return tuples;
    }

    @Override
    public List<String> getVariables() {
        if (variables == null){
            try{
                produceResults();
            } catch (SQLException e) {
                close();
                throw new QueryException(e);
            }
        }
        return variables;
    }
    
    private void produceResults() throws SQLException{
        rs = executeQuery(query);
        ResultSetMetaData md = rs.getMetaData();
        variables = new ArrayList<String>(md.getColumnCount());
        for (int i = 0; i < md.getColumnCount(); i++){
            variables.add(md.getColumnName(i+1));
        }
        tuples = new TupleResultIterator(stmt, rs, query, converter, variables, bindings);
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

}
