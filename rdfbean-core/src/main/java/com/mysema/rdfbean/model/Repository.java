/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.OutputStream;

import com.mysema.rdfbean.model.io.Format;

/**
 * Repository provides a general abstraction RDF persistence engines
 *
 * @author tiwe
 * @author sasa
 *
 */
public interface Repository {

    /**
     * Close the Repository and release related resources
     */
    void close();
    
    /**
     * Export the contents of the Repository 
     * 
     * @param format Format to be used
     * @param os target stream for output
     */
    void export(Format format, OutputStream os);

    /**
     * Initialize the Repository
     */
    void initialize();

    /**
     * Open a connection to the Repository
     * 
     * @return
     */
    RDFConnection openConnection();
    
    /**
     * @param operation
     * @return
     */
    void execute(Operation operation);
           
}