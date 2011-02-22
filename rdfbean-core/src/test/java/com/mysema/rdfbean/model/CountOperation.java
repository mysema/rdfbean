package com.mysema.rdfbean.model;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public class CountOperation implements RDFConnectionCallback<Long>{

    @Nullable
    private final ID subject;

    @Nullable
    private final UID predicate, context;

    @Nullable
    private final NODE object;

    private final boolean includeInferred;

    public CountOperation() {
        this(null, null, null, null, false);
    }

    public CountOperation(@Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, boolean includeInferred) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
        this.includeInferred = includeInferred;
    }

    @Override
    public Long doInConnection(RDFConnection connection) throws IOException {
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
