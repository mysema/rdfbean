package com.mysema.rdfbean.sesame;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;

/**
 * @author tiwe
 *
 */
public class TupleResultIterator implements CloseableIterator<Map<String, NODE>> {

    private final TupleResult tupleResult;
    
    private final SesameDialect dialect;
    
    public TupleResultIterator(TupleResult tupleResult, SesameDialect dialect) {
        this.tupleResult = tupleResult;
        this.dialect = dialect;
    }
    
    @Override
    public void close() {
        try {
            tupleResult.close();
        } catch (StoreException e1) {
            throw new RepositoryException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return tupleResult.hasNext();
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Map<String, NODE> next() {
        try {
            BindingSet bindingSet = tupleResult.next();
            Map<String,NODE> row = new HashMap<String,NODE>();
            for (String name : bindingSet.getBindingNames()){
                row.put(name, dialect.getNODE(bindingSet.getValue(name)));
            }
            return row;
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove() {

    }

}
