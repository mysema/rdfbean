package com.mysema.rdfbean.model.io;

import com.mysema.rdfbean.model.STMT;

/**
 * @author tiwe
 * 
 */
public interface RDFWriter {

    void begin();

    void handle(STMT stmt);

    void end();

}
