package com.mysema.rdfbean.sesame;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryEvaluationException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

public class BooleanQueryImpl implements SPARQLQuery{

    private final BooleanQuery booleanQuery;

    private final SesameDialect dialect;

    public BooleanQueryImpl(BooleanQuery query, SesameDialect dialect) {
        this.booleanQuery = query;
        this.dialect = dialect;
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
    public List<String> getVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
        try {
            return booleanQuery.evaluate();
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void setBinding(String variable, NODE node) {
        booleanQuery.setBinding(variable, dialect.getNode(node));
    }

    @Override
    public void setMaxQueryTime(int secs) {
        booleanQuery.setMaxQueryTime(secs);
    }

}
