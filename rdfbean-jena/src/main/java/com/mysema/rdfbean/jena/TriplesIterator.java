/**
 * 
 */
package com.mysema.rdfbean.jena;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public final class TriplesIterator implements CloseableIterator<STMT> {
        
    private final JenaDialect dialect;
    
    private final ExtendedIterator<Triple> triples;

    public TriplesIterator(JenaDialect dialect, ExtendedIterator<Triple> triples) {
        this.dialect = dialect;
        this.triples = triples;
    }

    @Override
    public void close() {
        triples.close();                
    }

    @Override
    public boolean hasNext() {
        return triples.hasNext();
    }

    @Override
    public STMT next() {
        Triple triple = triples.next();
        return new STMT(
                dialect.getID(triple.getSubject()),
                dialect.getUID(triple.getPredicate()),
                dialect.getNODE(triple.getObject()));
    }

    @Override
    public void remove() {
        triples.remove();
    }
}