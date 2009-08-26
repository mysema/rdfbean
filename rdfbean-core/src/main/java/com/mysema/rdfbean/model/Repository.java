/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

public interface Repository {

    void initialize();
    
    RDFConnection openConnection();

    void close();

}