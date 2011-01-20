package com.mysema.rdfbean.jena;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

public class BooleanQueryImpl implements SPARQLQuery {
    
    private final Query query;
    
    private final Model model;

    private final JenaDialect dialect;
    
    public BooleanQueryImpl(Query query, Model model, JenaDialect dialect) {
        this.query = query;
        this.model = model;
        this.dialect = dialect;
    }

    @Override
    public boolean getBoolean() {
        QueryExecution exec = QueryExecutionFactory.create(query, model);
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
    public List<String> getVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinding(String variable, NODE node) {
        // TODO
    }

    @Override
    public void setMaxQueryTime(int secs) {
        // TODO Auto-generated method stub
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

}
