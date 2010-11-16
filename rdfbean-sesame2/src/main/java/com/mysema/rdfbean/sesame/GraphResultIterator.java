package com.mysema.rdfbean.sesame;

import org.openrdf.result.GraphResult;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public class GraphResultIterator extends AbstractResultIterator{
    
    private final GraphResult graphResult;
    
    public GraphResultIterator(GraphResult graphResult, SesameDialect dialect){
        super(dialect);
        this.graphResult = graphResult;
    }
    

    @Override
    public void close(){
        try {
            graphResult.close();
        } catch (StoreException e1) {
            throw new RepositoryException(e1);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return graphResult.hasNext();
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public STMT next() {
        try {
            return convert(graphResult.next(), true);
        } catch (StoreException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
    


}
