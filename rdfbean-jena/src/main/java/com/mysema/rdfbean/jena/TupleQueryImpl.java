package com.mysema.rdfbean.jena;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 * 
 */
public class TupleQueryImpl extends AbstractQueryImpl {

    public TupleQueryImpl(Query query, Dataset dataset, JenaDialect dialect) {
        super(query, dataset, dialect);
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
        QueryExecution exec = createExecution();
        ResultSet resultSet = exec.execSelect();
        return new TupleResultIterator(resultSet, dialect);
    }

    @Override
    public List<String> getVariables() {
        return query.getResultVars();
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

}
