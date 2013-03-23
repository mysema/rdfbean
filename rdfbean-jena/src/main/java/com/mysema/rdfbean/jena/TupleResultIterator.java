package com.mysema.rdfbean.jena;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.NODE;

/**
 * @author tiwe
 * 
 */
public class TupleResultIterator implements CloseableIterator<Map<String, NODE>> {

    private final ResultSet resultSet;

    private final JenaDialect dialect;

    public TupleResultIterator(ResultSet resultSet, JenaDialect dialect) {
        this.resultSet = resultSet;
        this.dialect = dialect;
    }

    @Override
    public void close() {
        // ?!?
    }

    @Override
    public boolean hasNext() {
        return resultSet.hasNext();
    }

    @Override
    public Map<String, NODE> next() {
        QuerySolution solution = resultSet.next();
        Map<String, NODE> row = new HashMap<String, NODE>(resultSet.getResultVars().size());
        for (String var : resultSet.getResultVars()) {
            RDFNode node = solution.get(var);
            if (node != null) {
                row.put(var, dialect.getNODE(node.asNode()));
            }
        }
        return row;
    }

    @Override
    public void remove() {
        resultSet.remove();
    }

}
