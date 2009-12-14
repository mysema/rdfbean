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

/**
 * RDFConnection defines a session interface to the Repository
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface RDFConnection extends Closeable {

    CloseableIterator<STMT> findStatements(
            @Nullable ID subject, 
            @Nullable UID predicate, 
            @Nullable NODE object, 
            @Nullable UID context, boolean includeInferred);
    
    // Map<UID,Set<STMT>> findStatements(ID subject, Set<UID> directProps, 
    //     Set<UID> inverseProps, UID context, boolean includeInferred);

    void update(Set<STMT> removedStatements, Set<STMT> addedStatements);
    
    void clear();

    BeanQuery createQuery(Session session);
    
    <Q> Q createQuery(Session session, Class<Q> queryType);

    BID createBNode();

    RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel);

}
