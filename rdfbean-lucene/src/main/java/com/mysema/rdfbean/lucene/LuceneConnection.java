/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static com.mysema.rdfbean.lucene.Constants.ALL_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.BNODE_ID_PREFIX;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_NULL;
import static com.mysema.rdfbean.lucene.Constants.ID_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.TEXT_FIELD_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.util.EmptyCloseableIterator;
import com.mysema.util.ListMap;

/**
 * LuceneConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConnection implements RDFConnection{
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneConnection.class);

    private static final List<String> INTERNAL_FIELDS = Arrays.asList(
            "alias",
            "$/uid",
            ALL_FIELD_NAME, 
            CONTEXT_FIELD_NAME,
            ID_FIELD_NAME,
            TEXT_FIELD_NAME);
    
    private final Compass compass;
    
    private final CompassSession compassSession;
    
    private final LuceneConfiguration conf;
    
    public LuceneConnection(LuceneConfiguration configuration, Compass compass, CompassSession session) {
        this.conf = Assert.notNull(configuration);
        this.compass = Assert.notNull(compass);
        this.compassSession = Assert.notNull(session);
    }

    private String getPredicateField(UID predicate){
        return conf.getConverter().uidToShortString(predicate);
    }
    
    public void addStatement(Resource resource, STMT statement) {
        String predicateField = getPredicateField(statement.getPredicate());
        String objectValue = conf.getConverter().toString(statement.getObject());
        
        if (conf.isStored()){
            Property property = compass.getResourceFactory()
                .createProperty(predicateField, objectValue, Store.YES, Index.NOT_ANALYZED);
            resource.addProperty(property);
        }        
        
        if (conf.isAllIndexed()){
            resource.addProperty(ALL_FIELD_NAME, objectValue);    
        }        
        
        if (conf.isFullTextIndexed() && statement.getObject().isLiteral()){
            resource.addProperty(TEXT_FIELD_NAME, statement.getObject().getValue());   
        }        
    }

    @Override
    public RDFBeanTransaction beginTransaction(Session session,
            boolean readOnly, int txTimeout, int isolationLevel) {
        CompassTransaction tx = compassSession.beginTransaction();
        return new LuceneTransaction(tx);
    }
    
    @Override
    public void clear() {
        
        
    }
    
    @Override
    public void close() throws IOException {
        compassSession.close();
    }

    @Override
    public BID createBNode() {
        return new BID();
    }
    
    @Override
    public BeanQuery createQuery(Session session) {
        // TODO
        return null;
    }
    
    private Resource createResource(){
        return compass.getResourceFactory().createResource("resource");
    }
    
    private List<STMT> findStatements(Resource resource, ID subject, UID predicate, NODE object, UID context){
        List<STMT> stmts = new ArrayList<STMT>();
        ID sub = subject;
        UID pre = predicate;
        NODE obj = object;                    
        if (sub == null){
            sub = getId(resource.getId());
        }                    
        if (pre != null){
            if (obj != null){
                stmts.add(new STMT(sub, pre, obj));
            }else{
                for (Property property : resource.getProperties(getPredicateField(pre))){
                    obj = conf.getConverter().fromString(property.getStringValue());
                    stmts.add(new STMT(sub, pre, obj));
                }
            }
            
        }else if (obj != null){
            String objString = conf.getConverter().toString(obj);
            for (Property property : resource.getProperties()){
                if (!INTERNAL_FIELDS.contains(property.getName()) // better filtering 
                        && objString.equals(property.getStringValue())){
                    pre = conf.getConverter().uidFromShortString(property.getName());
                    stmts.add(new STMT(sub, pre, obj));
                }
            }
            
        }else{
            for (Property property : resource.getProperties()){
                if (!INTERNAL_FIELDS.contains(property.getName())){ // better filtering
                    pre = conf.getConverter().uidFromShortString(property.getName());
                    obj = conf.getConverter().fromString(property.getStringValue());
                    stmts.add(new STMT(sub, pre, obj));
                }    
            }            
        }
        return stmts;
    }
    
    @Override
    public CloseableIterator<STMT> findStatements(final ID subject, final UID predicate, final NODE object, 
            final UID context, boolean includeInferred) {        
        CompassQuery query = createQuery(subject, predicate, object, context);        
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
    
    long countDocuments(ID subject, UID predicate, NODE object, UID context){
        CompassQuery query = createQuery(subject, predicate, object, context);
        return query.count();
    }
    
    long countStatements(ID subject, UID predicate, NODE object, UID context){        
        try {
            CloseableIterator<STMT> stmts = findStatements(subject, predicate, object, context, false);
            long rv = 0;
            while (stmts.hasNext()){
                stmts.next();
                rv++;
            }
            stmts.close();
            return rv;
        } catch (IOException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        
    }
    
    private CompassQuery createQuery(ID subject, UID predicate, NODE object, UID context){
        CompassQueryBuilder queryBuilder = compassSession.queryBuilder();
        if (subject != null || predicate != null || object != null || context != null){            
            CompassBooleanQueryBuilder boolBuilder = queryBuilder.bool();
            if (subject != null){
                boolBuilder.addMust(queryBuilder.term(ID_FIELD_NAME, getId(subject)));
            }   
            if (predicate != null){
                String predicateField = conf.getConverter().uidToShortString(predicate);
                if (object != null){
                    String value = conf.getConverter().toString(object);
                    boolBuilder.addMust(queryBuilder.term(predicateField, value));    
                }else{
                    boolBuilder.addMust(queryBuilder.wildcard(predicateField, "*"));
                }
                
            }else if (object != null){
                String value = conf.getConverter().toString(object);
                boolBuilder.addMust(queryBuilder.term(ALL_FIELD_NAME, value));
            }
            
            if (context != null){
                String value = getContextId(context);
                boolBuilder.addMust(queryBuilder.term(CONTEXT_FIELD_NAME, value));
            }                       
            return boolBuilder.toQuery();
            
        }else{
            return queryBuilder.matchAll();
        }
    }
    
    private String getContextId(ID resource) {
        return resource == null ? CONTEXT_NULL : getId(resource);        
    }
        
    private String getId(ID resource) {
        Assert.notNull(resource);
        if (resource.isBNode()){
            return BNODE_ID_PREFIX + resource.getId();
        }else{
            return resource.getId();
        }
    }
    
    private ID getId(String id){
        Assert.notNull(id);
        if (id.startsWith(BNODE_ID_PREFIX)){
            return new BID(id.substring(1));
        }else{
            return new UID(id);
        }
    }
    
    private Resource getResource(String field, Object value) throws IOException {
        CompassHits hits = compassSession.queryBuilder().term(field, value).hits();
        return hits.getLength() > 0 ? hits.resource(0) : null;
    }
    
    private void update(ListMap<ID, STMT> rsAdded, ListMap<ID, STMT> rsRemoved,
            Set<ID> resources) throws IOException, CorruptIndexException {
        // for each resource, add/remove
        for (ID resource : resources) {
            // is the resource in the store?

            // fetch the Document representing this Resource
            String id = getId(resource);
//            Term idTerm = new Term(ID_FIELD_NAME, id);
            Resource luceneResource = getResource(ID_FIELD_NAME, id);

            if (luceneResource == null) {
                // there is no such Document: create one now
                luceneResource = createResource();
                luceneResource.addProperty(ID_FIELD_NAME, id);
                // add all statements, remember the contexts
                HashSet<ID> contextsToAdd = new HashSet<ID>();
                List<STMT> list = rsAdded.get(resource);
                if (list != null){
                    for (STMT s : list) {
                        addStatement(luceneResource, s);
                        contextsToAdd.add(s.getContext());
                    }
                }
                    
                if (conf.isContextsStored()){
                    // add all contexts
                    for (ID c : contextsToAdd) {
                        luceneResource.addProperty(CONTEXT_FIELD_NAME, getContextId(c));
                    }                    
                }
                
                // add it to the index
//                writer.addDocument(resource.getDocument());
                compassSession.save(luceneResource);

                if (rsRemoved.containsKey(resource)){
                    logger.warn(rsRemoved.get(resource).size() +
                        " statements are marked to be removed that should not be in the store," + 
                        " for resource " + resource + ". Nothing done.");
                }
                    
            } else {
                // update the Document

                // create a copy of the old document; updating the retrieved
                // Document instance works ok for stored properties but indexed data
                // gets lots when doing an IndexWriter.updateDocument with it
                Resource newResource = createResource();

                // buffer the removed literal statements
                ListMap<String, String> removedOfResource = null;
                {
                    List<STMT> removedStatements = rsRemoved.get(resource);
                    if (removedStatements != null) {
                        removedOfResource = new ListMap<String, String>();
                        for (STMT r : removedStatements) {
                            removedOfResource.put(r.getPredicate().getValue(), 
                                    conf.getConverter().toString(r.getObject()));
                        }
                    }
                }

                // add all existing fields (including id, context, and text)
                // but without adding the removed ones
                for (Property oldProperty : luceneResource.getProperties()) {
                    // do not copy removed statements to the new version of the
                    // document
                    if (removedOfResource != null) {
                        // which fields were removed?
                        List<String> objectsRemoved = removedOfResource.get(oldProperty.getName());
                        if ((objectsRemoved != null) && (objectsRemoved.contains(oldProperty.getStringValue()))) {
                            continue;
                        }

                    }
                    newResource.addProperty(oldProperty);
                }

                // add all statements to this document, remember the contexts
                {
                    List<STMT> addedToResource = rsAdded.get(resource);
                    if (addedToResource != null) {
                        HashSet<ID> contextsToAdd = new HashSet<ID>();
                        for (STMT s : addedToResource) {
                            addStatement(newResource, s);
                            contextsToAdd.add(s.getContext());
                        }
                        // add all contexts
                        for (ID c : contextsToAdd) {
                            newResource.addProperty(CONTEXT_FIELD_NAME, getContextId(c));
                        }
                    }
                }

                // update the index with the cloned document
//                writer.updateDocument(idTerm, newResource);
                compassSession.save(newResource);
            }
        }        
    }
        
    @Override
    public void update(Set<STMT> removed, Set<STMT> added) {
        // Buffer per resource
        ListMap<ID, STMT> rsAdded = new ListMap<ID, STMT>();
        ListMap<ID, STMT> rsRemoved = new ListMap<ID, STMT>();
        HashSet<ID> resources = new HashSet<ID>();
        for (STMT s : added) {
            rsAdded.put(s.getSubject(), s);
            resources.add(s.getSubject());
        }
        for (STMT s : removed) {
            rsRemoved.put(s.getSubject(), s);
            resources.add(s.getSubject());
        }

        try {
            update(rsAdded, rsRemoved, resources);
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
