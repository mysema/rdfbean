/**
 * 
 */
package com.mysema.rdfbean.jena;

import javax.annotation.Nullable;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 *
 */
public final class TriplesIterator implements CloseableIterator<STMT> {
        
    private final JenaDialect dialect;
    
    private final ExtendedIterator<Triple> triples;

    @Nullable
    private final UID context;
    
    public TriplesIterator(JenaDialect dialect, ExtendedIterator<Triple> triples, @Nullable UID context) {
        this.dialect = dialect;
        this.triples = triples;
        this.context = context;
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
                dialect.getNODE(triple.getObject()),
                context);
    }

    @Override
    public void remove() {
        triples.remove();
    }
}