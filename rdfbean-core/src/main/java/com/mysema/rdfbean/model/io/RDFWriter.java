package com.mysema.rdfbean.model.io;

import java.io.Closeable;

import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 *
 */
public interface RDFWriter extends Closeable{
    
    void start();
    
    void end();
    
    void namespace(String prefix, String namespace);
    
    void handle(STMT stmt);

}
