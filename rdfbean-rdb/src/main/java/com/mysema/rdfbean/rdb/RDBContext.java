/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.rdb.support.SortableQueryMetadata;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * RDBContext provides shared state and functionality for RDBCOnnection and RDBQuery
 *
 * @author tiwe
 * @version $Id$
 */
public final class RDBContext implements Closeable{
        
    private final ConverterRegistry converterRegistry;
    
    private final Connection connection;
    
    private final IdFactory idFactory;
    
    private final IdSequence idSequence;
    
    private final BidiMap<Locale,Integer> langCache;
    
    private final Map<Object,Long> idCache = new HashMap<Object,Long>(1000);
    
    private final BidiMap<NODE,Long> nodeCache;
    
    private final BidiMap<NODE,Long> localNodeCache = new DualHashBidiMap<NODE,Long>();
        
    private final Configuration configuration;
    
    public RDBContext(
            ConverterRegistry converterRegistry,
            IdFactory idFactory, 
            BidiMap<NODE,Long> nodeCache,  
            BidiMap<Locale,Integer> langCache,
            IdSequence idSequence,
            Connection connection, 
            SQLTemplates templates) {
        this.converterRegistry = converterRegistry;
        this.idFactory = idFactory;
        this.idSequence = idSequence;
        this.nodeCache = nodeCache;
        this.langCache = langCache;
        this.connection = connection;
        this.configuration = new Configuration(templates);
    }
    
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        try {
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
            if (isolationLevel != -1){
                connection.setTransactionIsolation(isolationLevel);    
            }else{
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }            
            return new RDBTransaction(connection);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }        
    }

    public void clear() {
        localNodeCache.clear();
    }
    
    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
    
    public SQLDeleteClause createDelete(RelationalPath<?> entity){
        return new SQLDeleteClause(connection, configuration, entity);
    }
    
    public SQLInsertClause createInsert(RelationalPath<?> entity){
        return new SQLInsertClause(connection, configuration, entity);
    }
    
    public SQLMergeClause createMerge(RelationalPath<?> entity){
        return new SQLMergeClause(connection, configuration, entity);
    }

    public SQLQuery createQuery(){
        return new SQLQueryImpl(connection, configuration, new SortableQueryMetadata());
    }

    public Long getId(Object constant) {        
        Long id = idCache.get(constant);
        if (id == null){
            UID type = converterRegistry.getDatatype(constant.getClass());
            String lexical = converterRegistry.toString(constant);
            id = getNodeId(new LIT(lexical, type));
            // Date is not immutable, so it can't be cached safely
            if (!java.util.Date.class.isAssignableFrom(constant.getClass())){
                idCache.put(constant, id);    
            }            
        }
        return id;
    }

    public ID getID(String lex){
        if (lex.contains(":")){
            return new UID(lex);            
        }else{
            return new BID(lex);
        }
    }

    @Nullable
    public Locale getLang(int id) {
        return langCache.getKey(id);
    }

    public Integer getLangId(Locale locale) {
        Integer id = langCache.get(locale);
        if (id == null){
            id = idFactory.getId(locale);
            langCache.put(locale, id);
        }
        return id;
    }

    public long getNextLocalId() {
        return idSequence.getNextId();
    }

    @Nullable
    public NODE getNode(long id, Transformer<Long,NODE> t) {
        NODE node = nodeCache.getKey(id);
        if (node == null){
            node = localNodeCache.getKey(id);
            if (node == null){
                node = t.transform(id);
                localNodeCache.put(node, id);
            }
        }        
        return node;
    }

    public Long getNodeId(NODE node) {
        Long id = nodeCache.get(node);
        if (id == null){
            id = localNodeCache.get(node);
            if (id == null){
                id = idFactory.getId(node);
                localNodeCache.put(node, id);
            }
        }
        return id;
    }
    
    public Collection<NODE> getNodes() {
        return nodeCache.keySet();
    }
    
    public <T> T convert(String value, Class<T> requiredType){
        return converterRegistry.fromString(value, requiredType);
    }
    
    public ConverterRegistry getConverters(){
        return converterRegistry;
    }
        
}
