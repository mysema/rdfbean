package com.mysema.rdfbean.sesame;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * @author tiwe
 * 
 */
public class TupleResultIterator implements CloseableIterator<Map<String, NODE>> {

    private final TupleQueryResult tupleResult;

    private final Map<String, NODE> bindings;

    private final SesameDialect dialect;


    public TupleResultIterator(TupleQueryResult tupleResult, Map<String, NODE> bindings, SesameDialect dialect) {
        this.tupleResult = tupleResult;
        this.bindings = bindings;
        this.dialect = dialect;
    }


    @Override
    public void close() {
        try {
            tupleResult.close();
        } catch (QueryEvaluationException e1) {
            throw new RepositoryException(e1);
        }
    }


    @Override
    public boolean hasNext() {
        try {
            return tupleResult.hasNext();
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public Map<String, NODE> next() {
        try {
            BindingSet bindingSet = tupleResult.next();
            Map<String, NODE> row = new HashMap<String, NODE>();
            for (String name : tupleResult.getBindingNames()) {
                Value value = bindingSet.getValue(name);
                if (value != null) {
                    row.put(name, dialect.getNODE(value));
                } else if (bindings.containsKey(name)) {
                    row.put(name, bindings.get(name));
                }
            }
            return row;
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public void remove() {
        try {
            tupleResult.remove();
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

}
