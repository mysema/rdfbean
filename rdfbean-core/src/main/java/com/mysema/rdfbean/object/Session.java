/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;

import com.mysema.query.grammar.types.Expr.EEntity;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;

/**
 * 
 * Session provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
public interface Session extends ObjectRepository {
    
    /**
     * 
     * @param instance
     * @return
     */
    LID save(Object instance);
    
    /**
     * 
     * @param instance
     * @return
     */
    List<LID> saveAll(Object... instance);

    /**
     * 
     * @param ns
     * @param parent
     */
    void addParent(String ns, ObjectRepository parent);
    
    /**
     * 
     * @param object
     */
    void autowire(Object object);

    /**
     * Begins a new read-write transaction with a default timeout. All session usage is within
     * transaction scope.
     *
     * @return the transaction
     *
     * @throws RDFBeanException on an error
     */
    public RDFBeanTransaction beginTransaction();

    /**
     * Begins a new transaction. All session usage is within transaction scope.
     *
     * @param readOnly  true if this should be a read-only transaction, false if a read-write
     *                  transaction
     * @param txTimeout the transaction timeout, in seconds; if &lt;= 0 then use the default timeout
     * @param isolationLevel the isolation level of the transaction
     * @return the transaction
     *
     * @throws RDFBeanException if a transaction is already active or if an error occurred starting
     *                      a new transaction
     */
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel);
    
    /**
     * 
     */
    void clear();

    /**
     * 
     */
    void close();
    
    /**
     * 
     * @param exprs
     * @return
     */
    BeanQuery from(EEntity<?>... exprs);

    /**
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    <T> List<T> findInstances(Class<T> clazz);

    /**
     * 
     * @param <T>
     * @param clazz
     * @param clazzUri
     * @return
     */
    <T> List<T> findInstances(Class<T> clazz, LID clazzUri);

    /**
     * 
     * @param <T>
     * @param clazz
     * @param clazzUri
     * @return
     */
    <T> List<T> findInstances(Class<T> clazz, UID clazzUri);
    
    /**
     * 
     * @param <T>
     * @param clazz
     * @param subject
     * @return
     */
    <T> T get(Class<T> clazz, ID subject);
    
    /**
     * 
     * @param <T>
     * @param clazz
     * @param subject
     * @return
     */
    <T> T get(Class<T> clazz, LID subject);
    
    /**
     * 
     * @param <T>
     * @param id
     * @param clazz
     * @return
     */
    <T> T getById(String id, Class<T> clazz);
    
    /**
     * 
     * @param <T>
     * @param <I>
     * @param clazz
     * @param subject
     * @return
     */
    <T, I extends ID> List<T> getAll(Class<T> clazz, I... subject);
    
    /**
     * 
     * @param <T>
     * @param clazz
     * @param subject
     * @return
     */
    <T> List<T> getAll(Class<T> clazz, LID... subject);
    
    /**
     * 
     * @param id
     * @return
     */
    LID getLID(ID id);
    
    /**
     * 
     * @return
     */
    boolean isReadOnly();
    
    /**
     * Get the current transaction or <code>null</code> if none is bound
     * 
     * @return
     */
    RDFBeanTransaction getTransaction();

}