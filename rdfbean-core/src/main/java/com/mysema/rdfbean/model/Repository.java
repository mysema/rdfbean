/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.OutputStream;

import com.mysema.rdfbean.model.io.Format;

public interface Repository {

    void close();
    
    void export(Format format, OutputStream os);

    void initialize();

    RDFConnection openConnection();
    
}