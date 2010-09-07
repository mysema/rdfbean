/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

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
     * Load the contents to the Repository
     * 
     * @param format Format to be used
     * @param is input stream
     * @param context target context or null for default
     * @param replace whether to replace the contents of the target context
     */
    void load(Format format, InputStream is, @Nullable UID context, boolean replace);
    
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
    <RT> RT execute(Operation<RT> operation);
           
}