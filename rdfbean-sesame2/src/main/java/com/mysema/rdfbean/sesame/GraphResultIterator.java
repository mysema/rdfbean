package com.mysema.rdfbean.sesame;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;

import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class GraphResultIterator extends AbstractResultIterator{
    
    private final GraphQueryResult graphResult;
    
    public GraphResultIterator(GraphQueryResult graphResult, SesameDialect dialect){
        super(dialect);
        this.graphResult = graphResult;
    }
    

    @Override
    public void close(){
        try {
            graphResult.close();
        } catch (QueryEvaluationException e1) {
            throw new RepositoryException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return graphResult.hasNext();
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public STMT next() {
        try {
            return convert(graphResult.next(), true);
        } catch (QueryEvaluationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
    


}
