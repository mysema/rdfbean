package com.mysema.rdfbean.model;

import java.io.IOException;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public class CountOperation implements Operation<Long>{

    private final ID subject;

    private final UID predicate, context;

    private final NODE object;

    private final boolean includeInferred;

    public CountOperation() {
        this(null, null, null, null, false);
    }

    public CountOperation(ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
        this.includeInferred = includeInferred;
    }

    @Override
    public Long execute(RDFConnection connection) throws IOException {
        long count = 0l;
        CloseableIterator<STMT> stmts = connection.findStatements(subject, predicate, object, context, includeInferred);
        try{
            while (stmts.hasNext()){
                count++;
                stmts.next();
            }
            return count;
        }finally{
            stmts.close();
        }
    }

}
