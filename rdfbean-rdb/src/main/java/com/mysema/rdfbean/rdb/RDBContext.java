/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import static com.mysema.rdfbean.rdb.QLanguage.language;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.rdb.support.DateTimeType;
import com.mysema.rdfbean.rdb.support.LocalDateType;
import com.mysema.rdfbean.rdb.support.LocalTimeType;
import com.mysema.rdfbean.xsd.DateConverter;
import com.mysema.rdfbean.xsd.TimestampConverter;

/**
 * RDBContext provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBContext implements Closeable{
    
    public static <T> Set<T> asSet(T... args){
        return new HashSet<T>(Arrays.asList(args));
    }
    
    private static final DateConverter dateConverter = new DateConverter();
    
    private static final TimestampConverter timestampConverter = new TimestampConverter();
    
    private static final Set<Class<?>> decimalClasses = RDBContext.<Class<?>>asSet(Double.class, Float.class, BigDecimal.class);    

    private static final Set<Class<?>> dateClasses = RDBContext.<Class<?>>asSet(java.sql.Date.class, LocalDate.class);
    
    private static final Set<Class<?>> dateTimeClasses = RDBContext.<Class<?>>asSet(java.util.Date.class, Timestamp.class, DateTime.class);
    
    private static final Set<Class<?>> timeClasses = RDBContext.<Class<?>>asSet(java.sql.Time.class, LocalTime.class);
    
    private static final Set<UID> decimalTypes = asSet(XSD.decimalType, XSD.doubleType, XSD.floatType);
    
    private static final Set<UID> integerTypes = asSet(XSD.integerType, XSD.longType, XSD.intType, XSD.shortType, XSD.byteType);
        
    private final Connection connection;
    
    private final IdFactory idFactory;
    
    private final IdSequence idSequence;
    
    private final BidiMap<Locale,Integer> langCache;
    
    private final BidiMap<NODE,Long> nodeCache;
    
    private final BidiMap<NODE,Long> localCache = new DualHashBidiMap<NODE,Long>();
        
    private final Configuration configuration;
    
    public RDBContext(IdFactory idFactory,
            BidiMap<NODE,Long> nodeCache,  
            BidiMap<Locale,Integer> langCache,
            IdSequence idSequence,
            Connection connection, 
            SQLTemplates templates) {
        this.idFactory = idFactory;
        this.idSequence = idSequence;
        this.nodeCache = nodeCache;
        this.langCache = langCache;
        this.connection = connection;
        this.configuration = new Configuration(templates);
        configuration.register(new DateTimeType());
        configuration.register(new LocalDateType());
        configuration.register(new LocalTimeType());
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
        localCache.clear();
    }
    
    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    public SQLDeleteClause createDelete(PEntity<?> entity){
        return new SQLDeleteClause(connection, configuration, entity);
    }
    
    public SQLInsertClause createInsert(PEntity<?> entity){
        return new SQLInsertClause(connection, configuration, entity);
    }
    
    public SQLMergeClause createMerge(PEntity<?> entity){
        return new SQLMergeClause(connection, configuration, entity);
    }

    public SQLQuery createQuery(){
        return new SQLQueryImpl(connection, configuration);
    }

    @Nullable
    public Locale getLang(int id) {
        return langCache.getKey(id);
    }

    public Integer getLangId(Locale locale) {
        Integer id =  langCache.get(locale);
        if (id == null){
            id = idFactory.getId(locale);
            SQLMergeClause merge = createMerge(language);
            merge.keys(language.id);
            merge.set(language.id, id);
            merge.set(language.text, LocaleUtil.toLang(locale));
            merge.execute();
            langCache.put(locale, id);
        }
        return id;
    }

    public long getNextLocalId() {
        return idSequence.getNextId();
    }

    @Nullable
    public NODE getNode(long id, Transformer<Long,NODE> t) {
        if (nodeCache.containsValue(id)){
            return nodeCache.getKey(id);
        }else if (localCache.containsValue(id)){
            return localCache.getKey(id);
        }else{
            NODE node = t.transform(id);
            localCache.put(node, id);
            return node;
        }
    }

    public Long getNodeId(NODE node) {
        if (nodeCache.containsKey(node)){
            return nodeCache.get(node);
        }else if (localCache.containsKey(node)){    
            return localCache.get(node);
        }else{
            Long id = idFactory.getId(node);
            localCache.put(node, id);
            return id;
        }        
    }

    public Collection<NODE> getNodes() {
        return nodeCache.keySet();
    }

    public boolean isDateTimeType(UID uid){
        return uid.equals(XSD.dateTime);
    }
    
    public boolean isDateType(UID uid) {
        return uid.equals(XSD.date);
    }
    
    public boolean isDecimalClass(Class<?> cl){
        return decimalClasses.contains(cl);
    }
    
    public boolean isDateClass(Class<?> cl){
        return dateClasses.contains(cl);
    }
    
    public boolean isTimeClass(Class<?> cl){
        return timeClasses.contains(cl);
    }
    
    public boolean isDateTimeClass(Class<?> cl){
        return dateTimeClasses.contains(cl);
    }

    public boolean isDecimalType(UID uid) {
        return decimalTypes.contains(uid);
    }

    public boolean isIntegerType(UID uid) {
        return integerTypes.contains(uid);
    }
    
    public java.sql.Date toDate(LIT literal){
        return dateConverter.fromString(literal.getValue());
    }
    
    public java.sql.Timestamp toTimestamp(LIT literal){
        return timestampConverter.fromString(literal.getValue());
    }
    
    public ID getID(String lexical, boolean resource){
        return resource ? new UID(lexical) : new BID(lexical);
    }

}
