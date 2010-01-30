/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.Closeable;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.IdentityService;

/**
 * Session provides an access point for persisting and querying persisted instances of mapped classes.
 * Classes are mapped using {@link ClassMapping} for types and e.g. {@link Predicate} for 
 * properties. 
 * <p>
 * Session provides
 * <ul>
 * <li>getters for known individuals</li>
 * <li>finders for all instances of a mapped class</li>
 * <li>means for creating type safe queries over domain model of mapped classes</li>
 * <li>means for persisting new instances as well as updating existing</li>
 * <li>manual transaction management</li>
 * <li>flush management</li>
 * <li>a mapping from a natural RDF resource id (URI or blank node) to a short 
 *     numeric local identifier (see {@link IdentityService}).</li>
 * <li>autowiring mapped properties of a transient instance</li>
 * </ul>
 *
 * @author sasa
 * @author tiwe
 * @version $Id$
 *
 */
public interface Session extends ObjectRepository, Closeable {
    
    /**
     * Registers a parent ObjectRepository for injections from a given namespace.
     * 
     * @param namespace  the namespace associated with given ObjectRepository.
     * @param parent    
     */
    void addParent(String namespace, ObjectRepository parent);
    
    /**
     * Binds mapped properties (e.g. default values) of a given object. The object doesn't 
     * have to be a mapped/managed class ({@link ClassMapping}) itself but if it has 
     * mapped properties, those are bound. 
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
     * Empties the primary cache and discards all unflushed changes.
     */
    void clear();
    
    /**
     * Deletes given object and all references to it.
     * 
     * @param object
     */
    void delete(Object object);
    
    /**
     * Deletes given objects and all references to those.
     * 
     * @param objects
     */
    void deleteAll(Object... objects);
    
    /**
     * Finds instances of a given mapped class.
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
    BeanQuery from(PEntity<?>... exprs);
    
    /**
     * Prepare a query of the given queryLanguage with the given definition
     * 
     * @param <D>
     * @param <Q>
     * @param queryLanguage
     * @param definition
     * @return
     */
    <D,Q> Q createQuery(QueryLanguage<D,Q> queryLanguage, D definition);
    
    /**
     * Prepare a query of the given queryLanguage without a definion
     * 
     * @param <Q>
     * @param queryLanguage
     */
    <Q> Q createQuery(QueryLanguage<Void,Q> queryLanguage);
    
    /**
     * Returns a bean of type clazz with the given natural id (URI or blank node) or null if not found.
     * 
     * @param clazz     type of queried object.
     * @param subject   ID of queried object.
     * @return          object with given id or null if not found.
     */
    @Nullable
    <T> T get(Class<T> clazz, ID subject);

    /**
     * Returns a bean of type clazz with the given local id or null if not found.
     * 
     * @param clazz     type of queried object.
     * @param subject   locla id of queried object.
     * @return          object with given id or null if not found.
     */
    @Nullable
    <T> T get(Class<T> clazz, LID subject);
    
    /**
     * Bulk fetch. See get(Class,ID). 
     * 
     * @param clazz
     * @param subject
     * @return
     */
    <T> List<T> getAll(Class<T> clazz, ID... subject);
    
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
    @Nullable
    <T> T getById(String id, Class<T> clazz);
    
    /**
     * @param <T>
     * @param example
     */
    @Nullable
    <T> T getByExample(T example);
    
    /**
     * @return  Configuration used by this session.
     */
    Configuration getConfiguration();

    /**
     * @param instance
     * @return
     */
    @Nullable
    ID getId(Object instance);
    
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
     * Sets the flushMode of this session.
     * 
     * @param flushMode 
     */
    void setFlushMode(FlushMode flushMode);

    /**
     * @return
     */
    Locale getCurrentLocale();

}