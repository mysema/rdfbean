/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.Closeable;
import java.util.Set;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;

public interface RDFConnection extends Closeable {

    CloseableIterator<STMT> findStatements(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, boolean includeInferred);

    void update(Set<STMT> removedStatements, Set<STMT> addedStatements);
    
    void clear();

    BeanQuery createQuery(Session session);

    BID createBNode();

    RDFBeanTransaction beginTransaction(Session session, boolean readOnly, int txTimeout, int isolationLevel);

}
