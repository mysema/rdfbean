/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

public interface Repository<T extends Dialect<?, ?, ?, ?, ?, ?>> {

    void add(STMT... stmts);

    T getDialect();

    RDFConnection openConnection();

}