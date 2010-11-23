package com.mysema.rdfbean.sesame;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.openrdf.query.TupleQuery;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.SPARQLQuery;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class TupleQueryImpl implements SPARQLQuery {

    private final Map<String, NODE> bindings = new HashMap<String, NODE>();

    private final SesameDialect dialect;

    private final TupleQuery query;

    @Nullable
    private TupleResult result;

    public TupleQueryImpl(TupleQuery query, SesameDialect dialect) {
        this.query = query;
        this.dialect = dialect;
    }

    @Override
    public boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    private TupleResult getResult() throws StoreException{
        if (result == null){
            result = query.evaluate();
        }
        return result;
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
        try {
            return new TupleResultIterator(getResult(), bindings, dialect);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public List<String> getVariables() {
        try {
            return getResult().getBindingNames();
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void streamTriples(Writer writer, String contentType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinding(String variable, NODE node) {
        bindings.put(variable, node);
        query.setBinding(variable, dialect.getNode(node));
    }

}
