package com.mysema.rdfbean.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.SPARQLQuery;

/**
 * @author tiwe
 *
 */
public abstract class AbstractQueryImpl implements SPARQLQuery{
    
    protected final Query query;
    
    private final Dataset dataset;
    
    protected final JenaDialect dialect;
    
    private final List<Var> vars = new ArrayList<Var>();
    
    private final Binding binding = new BindingMap();
    
    public AbstractQueryImpl(Query query, Dataset dataset, JenaDialect dialect) {
        this.query = query;
        this.dataset = dataset;
        this.dialect = dialect;
    }

    @Override
    public void setBinding(String variable, NODE node) {
        Var var = Var.alloc(variable); 
        vars.add(var);
        binding.add(var, dialect.getNode(node));
    }

    @Override
    public void setMaxQueryTime(int secs) {
        // TODO Auto-generated method stub
    }
    
    protected QueryExecution createExecution(){
        query.setBindings(vars, Collections.singletonList(binding));
        return QueryExecutionFactory.create(query, dataset);
    }
}
