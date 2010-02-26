/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static com.mysema.rdfbean.lucene.Constants.ALL_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_NULL;
import static com.mysema.rdfbean.lucene.Constants.EMBEDDED_ID_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.ID_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.TEXT_FIELD_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.lucene.index.CorruptIndexException;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.CompassQueryBuilder.CompassBooleanQueryBuilder;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.EmptyCloseableIterator;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.NodeType;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnsupportedQueryLanguageException;
import com.mysema.rdfbean.object.QueryLanguage;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SimpleBeanQuery;
import com.mysema.util.ListMap;

/**
 * LuceneConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
// TODO : clean this up!
class LuceneConnection implements RDFConnection{
    
    private static final List<String> INTERNAL_FIELDS = Arrays.asList(
            "alias",
            "$/uid",
            ALL_FIELD_NAME, 
            CONTEXT_FIELD_NAME,
            ID_FIELD_NAME,
            TEXT_FIELD_NAME);
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneConnection.class);
    
    private final Compass compass;
    
    private final CompassSession compassSession;
    
    private final LuceneConfiguration conf;
    
    private final NodeConverter converter;
    
    @Nullable
    private LuceneTransaction localTxn = null;
    
    private boolean readonlyTnx = false;
        
    public LuceneConnection(LuceneConfiguration configuration, CompassSession session) {
        this.conf = Assert.notNull(configuration);
        this.converter = conf.getConverter();
        this.compassSession = Assert.notNull(session);
        this.compass = conf.getCompass();
    }
    
    private void addStatement(Resource resource, boolean component, STMT stmt, List<ID> subjectTypes) {        
//        System.err.println("added : " + stmt);
        String objectValue = converter.toString(stmt.getObject());        
        PropertyConfig propertyConfig = conf.getPropertyConfig(stmt.getPredicate(), subjectTypes);
        
        if (propertyConfig != null){           
            // index predicate
            if (propertyConfig.getStore() != Store.NO || propertyConfig.getIndex() != Index.NO){
                String predicateField = converter.toString(stmt.getPredicate());
                if (component){
                    predicateField = converter.toString(stmt.getSubject()) + " " + predicateField; 
                }                
                Property property = compass.getResourceFactory().createProperty(predicateField, objectValue, 
                        propertyConfig.getStore(), propertyConfig.getIndex());
                resource.addProperty(property);
                
                // index supertypes
                if (conf.isIndexSupertypes() && stmt.getPredicate().equals(RDF.type) && stmt.getObject() instanceof ID){
                    for (ID supertype : conf.getSupertypes((ID) stmt.getObject())){            
                        if (!supertype.equals(stmt.getObject())){
                            String supertypeValue = converter.toString(supertype);
                            resource.addProperty(compass.getResourceFactory().createProperty(predicateField, supertypeValue, 
                                    Store.NO, Index.NOT_ANALYZED));    
                        }                        
                    }
                }
                
            }     
            
            // index value into all field
            if (propertyConfig.isAllIndexed()){
                resource.addProperty(ALL_FIELD_NAME, objectValue);
            }            
            
            // index value into text field
            if (propertyConfig.isTextIndexed()){
                String value;
                if (conf.isLocalNameAsText() && stmt.getObject().getNodeType() == NodeType.URI){
                    value = ((UID)stmt.getObject()).getLocalName();
                }else{
                    value = stmt.getObject().getValue();
                }
                resource.addProperty(TEXT_FIELD_NAME, value);
            }            
        }
        
    }   

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly,
            int txTimeout, int isolationLevel) {
        CompassTransaction tx = compassSession.beginTransaction();
        readonlyTnx = readOnly;
        localTxn = new LuceneTransaction(this, tx);        
        return localTxn;
    }
    
    public void cleanUpAfterCommit(){
        localTxn = null;
        readonlyTnx = false;
    }
    
    public void cleanUpAfterRollback(){
        localTxn = null;
        readonlyTnx = false;
        // NOTE : LuceneConnection is closed after rollback
        close();
    }
    
    @Override
    public void clear() {
        compassSession.evictAll();        
    }

    @Override
    public void close()  {
        compassSession.close();
    }
    
    @Override
    public BID createBNode() {
        return new BID();
    }
    
    private CompassQuery createQuery(ID subject, UID predicate, NODE object, UID context, boolean includeInferred){
        CompassQueryBuilder queryBuilder = compassSession.queryBuilder();
        if (subject != null || predicate != null || object != null || context != null){            
            CompassBooleanQueryBuilder boolBuilder = queryBuilder.bool();
            if (subject != null){
                String subjectStr = converter.toString(subject);
                if (conf.isEmbeddedIds()){
                    boolBuilder.addMust(queryBuilder.term(EMBEDDED_ID_FIELD_NAME, subjectStr));    
                }else{
                    boolBuilder.addMust(queryBuilder.term(ID_FIELD_NAME, subjectStr));
                }
            }   
            if (predicate != null){
                String predicateField = converter.toString(predicate);
                // TODO : component predicate matches need to be handled here
                if (object != null){
                    String value = converter.toString(object);
                    boolBuilder.addMust(queryBuilder.term(predicateField, value));                        
                }else{
                    boolBuilder.addMust(queryBuilder.wildcard(predicateField, "*"));
                }
                
            }else if (object != null){
                String value = converter.toString(object);
                boolBuilder.addMust(queryBuilder.term(ALL_FIELD_NAME, value));
            }
            
            if (conf.isContextsStored()){
                if (context != null){
                    String value = converter.toString(context);
                    boolBuilder.addMust(queryBuilder.term(CONTEXT_FIELD_NAME, value));
                }else{
                    boolBuilder.addMust(queryBuilder.term(CONTEXT_FIELD_NAME, CONTEXT_NULL));
                }
            }
                                   
            return boolBuilder.toQuery();
            
        }else{
            return queryBuilder.matchAll();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, Q> Q createQuery(Session session, QueryLanguage<D, Q> queryLanguage, D definition) {
        if (queryLanguage.equals(QueryLanguage.QUERYDSL)){
            return (Q) new SimpleBeanQuery(session);
        }else if (queryLanguage.equals(Constants.LUCENEQUERY)){
            return (Q) new LuceneQuery(conf, session, compassSession);
        }else if (queryLanguage.equals(Constants.COMPASSQUERY)){    
            return (Q) compassSession.queryBuilder();
        }else{
            throw new UnsupportedQueryLanguageException(queryLanguage);
        }
    }
        
    private Resource createResource(){
        return compass.getResourceFactory().createResource("resource");
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(final ID subject, final UID predicate, final NODE object, 
            final UID context, boolean includeInferred) {        
        CompassQuery query = createQuery(subject, predicate, object, context, includeInferred);        
        CompassHits hits = query.hits();        
        if (hits.getLength() > 0){
            return new ResultIterator(hits){
                @Override
                protected List<STMT> getStatements(Resource resource) {
                    return findStatements(resource, subject, predicate, object, context);
                }                
            };
        }else{
            hits.close();
            return new EmptyCloseableIterator<STMT>();
        }
    }

    protected List<STMT> findStatements(Resource resource, ID subject, UID predicate, NODE object, UID context){
        // TODO : how to handle embedded properties of components?!?
        List<STMT> stmts = new ArrayList<STMT>();
        ID sub = subject;
        UID pre = predicate;
        NODE obj = object;               
        if (sub == null){
            sub = (ID) converter.fromString(resource.getId());
        }                    
        if (pre != null){
            if (obj != null){
                stmts.add(new STMT(sub, pre, obj));
            }else{
                for (Property property : resource.getProperties(getPredicateField(pre))){
                    obj = converter.fromString(property.getStringValue());
                    stmts.add(new STMT(sub, pre, obj));
                }
            }
            
        }else if (obj != null){
            String objString = converter.toString(obj);
            for (Property property : resource.getProperties()){
                if (isPredicateProperty(property.getName())  && objString.equals(property.getStringValue())){
                    pre = (UID) converter.fromString(property.getName());
                    stmts.add(new STMT(sub, pre, obj));
                }
            }
            
        }else{
            for (Property property : resource.getProperties()){
                if (isPredicateProperty(property.getName())){ 
                    pre = (UID) converter.fromString(property.getName());
                    obj = converter.fromString(property.getStringValue());
                    stmts.add(new STMT(sub, pre, obj));
                }    
            }            
        }
        return stmts;
    }
          
    private String getPredicateField(UID predicate){
        return converter.toString(predicate);
    }
    
    private Resource getResource(String field, Object value) throws IOException {
        CompassHits hits = compassSession.queryBuilder().term(field, value).hits();
        return hits.getLength() > 0 ? hits.resource(0) : null;
    }
        
    private List<ID> getSubjectTypes(ID subject, ListMap<ID, ID> types, Resource luceneResource) {
        List<ID> subjectTypes = types.get(subject);
        if (subjectTypes == null){
            List<STMT> typeStmts = findStatements(luceneResource, subject, RDF.type, null, null); 
            subjectTypes = new ArrayList<ID>(typeStmts.size());
            for (STMT stmt : typeStmts){
                subjectTypes.add((ID) stmt.getObject());
            }
            types.put(subject, subjectTypes);
        }
        return subjectTypes;
    }

    private boolean isPredicateProperty(String fieldName){
        return !INTERNAL_FIELDS.contains(fieldName) && !fieldName.contains(" ");
    }

    private void removeStatement(Resource resource, boolean component, STMT stmt, List<ID> subjectTypes){
//        System.err.println("removed : " + stmt);
        String objectValue = converter.toString(stmt.getObject());
        PropertyConfig propertyConfig = conf.getPropertyConfig(stmt.getPredicate(), subjectTypes);
        
        if (propertyConfig != null){
            if (propertyConfig.getStore() != Store.NO || propertyConfig.getIndex() != Index.NO){
                String predicateField = converter.toString(stmt.getPredicate());
                if (component){
                    predicateField = converter.toString(stmt.getSubject()) + " " + predicateField; 
                }                
                
                List<Property> properties = new ArrayList<Property>(Arrays.asList(resource.getProperties(predicateField)));
                for (Property property : properties.toArray(new Property[0])){
                    if (property.getStringValue().equals(objectValue)){
                        properties.remove(property);
                    }
                }   
                // remove all predicate
                resource.removeProperty(predicateField);
                // add left ones back
                for (Property left : properties){
                    resource.addProperty(left);
                }
            }
            
            if (propertyConfig.isAllIndexed()){
                // TODO : handle ALL indexed                
            }

            if (propertyConfig.isTextIndexed()){
                // TODO : handled TEXT indexed
            }
        }
    }
        
    private void update(ListMap<ID,ID> types, ListMap<ID, STMT> rsAdded, ListMap<ID, STMT> rsRemoved,
            Set<ID> resources) throws IOException, CorruptIndexException {

        for (ID resource : resources) {
            String id = converter.toString(resource);
            Resource luceneResource = null;
            if (conf.isEmbeddedIds()){
                luceneResource = getResource(EMBEDDED_ID_FIELD_NAME, id);
            }else{
                luceneResource = getResource(ID_FIELD_NAME, id);
            }

            if (luceneResource == null){
                luceneResource = createResource();
                luceneResource.addProperty(ID_FIELD_NAME, id);
            }
            
            // removed
            List<STMT> removedStatements = rsRemoved.get(resource);
            if (removedStatements != null) {
                for (STMT stmt : removedStatements) {
                    List<ID> subjectTypes = getSubjectTypes(stmt.getSubject(), types, luceneResource);
                    removeStatement(luceneResource, !stmt.getSubject().equals(resource), stmt, subjectTypes);
                }
            }
            
            // added
            List<STMT> addedToResource = rsAdded.get(resource);
            if (addedToResource != null) {
                for (STMT stmt : addedToResource) {
                    List<ID> subjectTypes = getSubjectTypes(stmt.getSubject(), types, luceneResource);
                    addStatement(luceneResource, !stmt.getSubject().equals(resource), stmt, subjectTypes);
                }
            }

            if (luceneResource.getProperties(converter.toString(RDF.type)).length > 0){
                compassSession.save(luceneResource);    
            }else{
                compassSession.delete(luceneResource);
            }
            
        }        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(Set<STMT> removed, Set<STMT> added) {
        if (!readonlyTnx){
            ListMap<ID, STMT> rsAdded = new ListMap<ID, STMT>();
            ListMap<ID, STMT> rsRemoved = new ListMap<ID, STMT>();
            ListMap<ID, ID> types = new ListMap<ID, ID>();
            HashSet<ID> resources = new HashSet<ID>();

            Map<ID,ID> componentToHost;
            if (!conf.getComponentProperties().isEmpty()){
                componentToHost = new HashMap<ID,ID>();
                for (Set<STMT> stmts : Arrays.asList(added, removed)){
                    for (STMT s :stmts){
                        if (conf.getComponentProperties().contains(s.getPredicate())){
                            componentToHost.put((ID) s.getObject(), s.getSubject());
                        }
                    }   
                }
            }else{
                componentToHost = Collections.emptyMap();
            }
            
            // populate rsAdded and rsRemoved
            for (Set<STMT> stmts : Arrays.asList(added ,removed)){
                ListMap<ID,STMT> target = stmts == added ? rsAdded : rsRemoved;
                for (STMT s : stmts){
                    if (componentToHost.containsKey(s.getSubject())){
                        target.put(componentToHost.get(s.getSubject()), s);    
                    }else{
                        target.put(s.getSubject(), s);
                    }                
                    resources.add(s.getSubject());
                    if (s.getPredicate().equals(RDF.type) && stmts == added){
                        if (s.getObject().isResource()){
                            types.put(s.getSubject(), (ID) s.getObject());    
                        }                        
                    }
                }                                  
            }
            
            try {
                update(types, rsAdded, rsRemoved, resources);
            } catch (CorruptIndexException e) {
                String error = "Caught " + e.getClass().getName();
                logger.error(error, e);
                throw new RuntimeException(error, e);
            } catch (IOException e) {
                String error = "Caught " + e.getClass().getName();
                logger.error(error, e);
                throw new RuntimeException(error, e);
            }
        }                
    }

    @Override
    public synchronized long getNextLocalId() {
        return conf.getNextLocalId();
    }
}
