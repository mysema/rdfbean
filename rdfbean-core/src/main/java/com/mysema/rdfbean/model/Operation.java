package com.mysema.rdfbean.model;

import java.io.IOException;

/**
 * @author tiwe
 *
 */
public interface Operation {

    /**
     * @return
     * @throws IOException 
     */
    void execute(RDFConnection connection) throws IOException;
    
}
