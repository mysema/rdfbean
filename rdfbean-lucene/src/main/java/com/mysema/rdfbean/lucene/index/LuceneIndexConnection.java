package com.mysema.rdfbean.lucene.index;

import org.compass.core.CompassSession;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.lucene.AbstractLuceneConnection;
import com.mysema.rdfbean.lucene.LuceneConfiguration;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * LuceneIndexConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneIndexConnection extends AbstractLuceneConnection{

    public LuceneIndexConnection(LuceneConfiguration configuration, CompassSession session) {
        super(configuration, session);
    }

    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BID createBNode() {
        throw new UnsupportedOperationException();
    }
}
