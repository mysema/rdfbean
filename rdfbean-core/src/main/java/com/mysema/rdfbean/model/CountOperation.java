package com.mysema.rdfbean.model;

import java.io.IOException;

import com.mysema.commons.lang.CloseableIterator;

/**
 * @author tiwe
 *
 */
public class CountOperation implements Operation<Long>{

    @Override
    public Long execute(RDFConnection connection) throws IOException {
        long count = 0l;
        CloseableIterator<STMT> stmts = connection.findStatements(null, null, null, null, false);
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
