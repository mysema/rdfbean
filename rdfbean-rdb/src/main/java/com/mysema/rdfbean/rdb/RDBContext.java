package com.mysema.rdfbean.rdb;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryImpl;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.model.IdSequence;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * RDBContext provides
 *
 * @author tiwe
 * @version $Id$
 */
public class RDBContext implements Closeable{
    
    private static final List<UID> decimalTypes = Arrays.asList(XSD.decimalType, XSD.doubleType, XSD.floatType);
    
    private static final List<UID> integerTypes = Arrays.asList(XSD.integerType, XSD.intType, XSD.byteType, XSD.longType);
    
    private final Connection connection;
    
    private final IdFactory idFactory;
    
    private final IdSequence idSequence;
    
    private final Map<Locale,Integer> langCache;
    
    private final Map<NODE,Long> nodeCache;
    
    private final Map<NODE,Long> localNodeCache = new HashMap<NODE,Long>();
    
    private final SQLTemplates templates;
    
    public RDBContext(IdFactory idFactory,
            Map<NODE,Long> nodeCache,  
            Map<Locale,Integer> langCache,
            IdSequence idSequence,
            Connection connection, 
            SQLTemplates templates) {
        this.idFactory = idFactory;
        this.idSequence = idSequence;
        this.nodeCache = nodeCache;
        this.langCache = langCache;
        this.connection = connection;
        this.templates = templates;
    }
    
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        try {
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
            connection.setTransactionIsolation(isolationLevel);
            return new RDBTransaction(connection);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }        
    }

    public void clear() {
        localNodeCache.clear();
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
        return new SQLDeleteClause(connection, templates, entity);
    }
    
    public SQLInsertClause createInsert(PEntity<?> entity){
        return new SQLInsertClause(connection, templates, entity);
    }
    
    public SQLMergeClause createMerge(PEntity<?> entity){
        return new SQLMergeClause(connection, templates, entity);
    }

    public SQLQuery createQuery(){
        return new SQLQueryImpl(connection, templates);
    }

    public Integer getLangId(Locale lang) {
        return langCache.get(lang);
    }

    public long getNextLocalId() {
        return idSequence.getNextId();
    }

    public Long getNodeId(NODE node) {
        if (nodeCache.containsKey(node)){
            return nodeCache.get(node);
        }else if (localNodeCache.containsKey(node)){    
            return localNodeCache.get(node);
        }else{
            Long id = idFactory.getId(node);
            localNodeCache.put(node, id);
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

    public boolean isDecimalType(UID uid) {
        return decimalTypes.contains(uid);
    }
    
    public boolean isIntegerType(UID uid) {
        return integerTypes.contains(uid);
    }

}
