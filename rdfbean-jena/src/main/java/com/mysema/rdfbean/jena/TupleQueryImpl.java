package com.mysema.rdfbean.jena;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

public class TupleQueryImpl implements SPARQLQuery {

    private final Query query;
    
    private final Model model;
    
    private final JenaDialect dialect;
    
    public TupleQueryImpl(Query query, Model model, JenaDialect dialect) {
        this.query = query;
        this.model = model;
        this.dialect = dialect;
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
        QueryExecution exec = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = exec.execSelect();
        return new TupleResultIterator(query, resultSet, dialect);
    }

    @Override
    public List<String> getVariables() {
        return query.getResultVars();
    }

    @Override
    public void setBinding(String variable, NODE node) {
        // TODO Auto-generated method stub
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
