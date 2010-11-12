/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import java.io.Closeable;
import java.util.Collection;

import javax.annotation.Nullable;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.object.Session;

/**
 * RDFConnection defines a session interface to the Repository
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface RDFConnection extends Closeable{

    /**
     * Create a new transaction for the Connection
     *
     * @param readOnly
     * @param txTimeout
     * @param isolationLevel
     * @return
     */
    RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel);

    /**
     * Clear any cached objects in the Connection
     */
    void clear();

    /**
     * Close this connection
     */
    void close();

    /**
     * Create a new unique Blank node
     *
     * @return
     */
    BID createBNode();

    /**
     * Prepare a Query of the given query language with the given definition
     *
     * @param <D>
     * @param <Q>
     * @param queryLanguage
     * @param definition
     * @return
     */
    <D,Q> Q createQuery(QueryLanguage<D,Q> queryLanguage, @Nullable D definition);

    /**
     * Prepare a Query of the given query language with the given definition
     *
     * @param <D>
     * @param <Q>
     * @param session
     * @param queryLanguage
     * @param definition
     * @return
     */
    <D,Q> Q createQuery(Session session, QueryLanguage<D,Q> queryLanguage, @Nullable D definition);

    /**
     * Find the statements matching the given pattern
     *
     * @param subject
     * @param predicate
     * @param object
     * @param context
     * @param includeInferred true, if inferred triples are included, and false, if not
     * @return
     */
    CloseableIterator<STMT> findStatements(
            @Nullable ID subject,
            @Nullable UID predicate,
            @Nullable NODE object,
            @Nullable UID context, boolean includeInferred);

    /**
     * Find out if statements matching the given pattern exist
     *
     * @param subject
     * @param predicate
     * @param object
     * @param context
     * @param includeInferred
     * @return
     */
    boolean exists(
            @Nullable ID subject,
            @Nullable UID predicate,
            @Nullable NODE object,
            @Nullable UID context, boolean includeInferred);

    /**
     * Get a unallocated local id for use in a ID/LID mapping
     *
     * @return
     */
    long getNextLocalId();

    /**
     * Update the Repository with the given statements
     *
     * @param removedStatements statements to be removed
     * @param addedStatements statement to be added
     */
    void update(@Nullable Collection<STMT> removedStatements, @Nullable Collection<STMT> addedStatements);

}
