/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.List;

import com.mysema.query.grammar.types.Expr.EEntity;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * Session provides access point for persisting and querying persisted instances of mapped classes.
 * Classes are mapped using {@link ClassMapping} for types and e.g. {@link Predicate} for 
 * properties. 
 * <p>
 * Session provides
 * <ul>
 * <li>Getters for known individuals</li>
 * <li>Finders for all instances of a mapped class</li>
 * <li>Means for creating type safe queries over domain model of mapped classes</li>
 * <li>Means for persisting new instances as well as updating existing</li>
 * <li>Manual transaction management</li>
 * <li>Flush management</li>
 * <li>A mapping from a natural RDF resource id (URI or blank node) to a short 
 *     numeric local identifier (see {@link IdentityService}).</li>
 * <li>Autowiring mapped properties of a transient instance</li>
 * </ul>
 *
 * @author sasa
 * @author tiwe
 * @version $Id$
 *
 */
public interface Session extends ObjectRepository {
    
    /**
     * Registers parent ObjectRepository for injections from given namespace.
     * 
     * @param ns        the namespace associated with given ObjectRepository.
     * @param parent    
     */
    void addParent(String ns, ObjectRepository parent);
    
    /**
     * Binds mapped properties (e.g. default values) of given object. Object doesn't 
     * have to be a mapped/managed class ({@link ClassMapping}) itself but if it has 
     * mapped properties, those are binded. 
     * 
     * @param object
     */
    void autowire(Object object);

    /**
     * Begins a new read-write transaction with a default timeout and binds this session to it.
     *
     * @return the transaction
     *
     * @throws RDFBeanException on an error
     */
    public RDFBeanTransaction beginTransaction();
    
    /**
     * Begins a new transaction and binds this session to it.
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
     * Empties primary cache and discards all unflushed changes.
     */
    void clear();

    /**
     * Closes this session and releases all held resources (e.g. database connections or file locks).
     */
    void close();
    
    /**
     * Finds instances of given mapped class.
     * 
     * @param clazz     mapped class of instances to be returned.
     * @return          all instances of given class.
     */
    <T> List<T> findInstances(Class<T> clazz);

    /**
     * Find instances that are of rdf:type clazzUri and assignable from clazz. 
     * 
     * @param clazz     class of instances requested.
     * @param clazzUri  local id of rdf:type of instances requested.
     * @return
     */
    <T> List<T> findInstances(Class<T> clazz, LID clazzUri);
    
    /**
     * Find instances that are of rdf:type clazzUri and assignable from clazz. 
     * 
     * @param clazz     class of instances requested.
     * @param clazzUri  rdf:type of instances requested.
     * @return
     */
    <T> List<T> findInstances(Class<T> clazz, UID clazzUri);

    /**
     * Flushes buffered changes to underlying repository.
     */
    void flush();
    
    /**
     * Creates a BeanQuery through which managed beans can be searched.
     * 
     * @param exprs     Querydsl source expressions.
     * @return
     */
    BeanQuery from(EEntity<?>... exprs);

    /**
     * Returns a bean of type clazz with given natural id (URI or blank node) or null if not found.
     * 
     * @param clazz     type of queried object.
     * @param subject   ID of queried object.
     * @return          object with given id or null if not found.
     */
    <T> T get(Class<T> clazz, ID subject);

    /**
     * Returns a bean of type clazz with given local id or null if not found.
     * 
     * @param clazz     type of queried object.
     * @param subject   locla id of queried object.
     * @return          object with given id or null if not found.
     */
    <T> T get(Class<T> clazz, LID subject);
    
    /**
     * Bulk fetch. See get(Class,ID). 
     * 
     * @param clazz
     * @param subject
     * @return
     */
    <T, I extends ID> List<T> getAll(Class<T> clazz, I... subject);
    
    /**
     * Bulk fetch. See get(Class,LID). 
     * 
     * @param clazz
     * @param subject
     * @return
     */
    <T> List<T> getAll(Class<T> clazz, LID... subject);
    
    /**
     * A shortcut for getting identified object with given local id. See get(Class,LID).
     * 
     * @param id        Local id of queried object.
     * @param clazz
     * @return
     */
    <T> T getById(String id, Class<T> clazz);
    
    /**
     * Returns the local id (LID) corresponding to given natural id. 
     * 
     * @param id    natural id, URI or blank node
     * @return
     */
    LID getLID(ID id);
    
    /**
     * Get the current transaction or <code>null</code> if none is bound
     * 
     * @return
     */
    RDFBeanTransaction getTransaction();
    
    /**
     * @return  true if this session is read-only.
     */
    boolean isReadOnly();
    
    /**
     * Saves (create or update) the given instance and returns an id assigned to it. 
     * <p>
     * NOTE: Changes are not actually persisted until flush is called. See {@link FlushMode}.
     * 
     * @param instance  instance of a mapped class to be persisted/updated.
     * @return          a local id assigned for given instance.
     */
    LID save(Object instance);
    
    /**
     * Bulk save. See save(Object). 
     * 
     * @param instance  instances to be persisted/updated.
     * @return
     */
    List<LID> saveAll(Object... instance);

    /**
     * @return  current flush mode of this session.
     */
    FlushMode getFlushMode();

    /**
     * Set's the flushMode of this session.
     * 
     * @param flushMode 
     */
    void setFlushMode(FlushMode flushMode);

}