package com.mysema.rdfbean.jena;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 * 
 */
public class BooleanQueryImpl extends AbstractQueryImpl {

    public BooleanQueryImpl(Query query, Dataset dataset, JenaDialect dialect) {
        super(query, dataset, dialect);
    }

    @Override
    public boolean getBoolean() {
        QueryExecution exec = createExecution();
        return exec.execAsk();
    }

    @Override
    public ResultType getResultType() {
        return ResultType.BOOLEAN;
    }

    @Override
    public CloseableIterator<STMT> getTriples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloseableIterator<Map<String, NODE>> getTuples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getVariables() {
        throw new UnsupportedOperationException();
    }
}
