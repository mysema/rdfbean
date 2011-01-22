/**
 * 
 */
package com.mysema.rdfbean.jena;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Quad;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public final class QuadsIterator implements CloseableIterator<STMT> {
        
    private final JenaDialect dialect;
    
    private final Iterator<Quad> quads;

    public QuadsIterator(JenaDialect dialect, Iterator<Quad> quads) {
        this.dialect = dialect;
        this.quads = quads;
    }

    @Override
    public void close() {
        // ?!?                
    }

    @Override
    public boolean hasNext() {
        return quads.hasNext();
    }

    @Override
    public STMT next() {
        Quad quad = quads.next();
        return new STMT(
            dialect.getID(quad.getSubject()),
            dialect.getUID(quad.getPredicate()),
            dialect.getNODE(quad.getObject()),
            quad.getGraph() != null ? dialect.getUID(quad.getGraph()) : null);
    }

    @Override
    public void remove() {
        quads.remove();
    }
}