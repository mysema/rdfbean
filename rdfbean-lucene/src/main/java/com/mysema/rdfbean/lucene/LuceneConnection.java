/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static com.mysema.rdfbean.lucene.Constants.BNODE_ID_PREFIX;
import static com.mysema.rdfbean.lucene.Constants.CONTEXT_NULL;
import static com.mysema.rdfbean.lucene.Constants.ID_FIELD_NAME;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
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
import com.mysema.util.ListMap;

/**
 * LuceneConnection provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneConnection implements RDFConnection{
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneConnection.class);
    
    private final LuceneConfiguration conf;
    
    private final IndexSearcher searcher;
    
    private final IndexWriter writer;
    
    public LuceneConnection(LuceneConfiguration configuration, IndexWriter writer, IndexSearcher searcher) {
        this.conf = Assert.notNull(configuration);
        this.writer = Assert.notNull(writer);
        this.searcher = Assert.notNull(searcher);
    }

    @Override
    public RDFBeanTransaction beginTransaction(Session session,
            boolean readOnly, int txTimeout, int isolationLevel) {
        return new LuceneTransaction(writer);
    }

    @Override
    public void clear() {
        // TODO
        
    }
    
    @Override
    public void close() throws IOException {
        writer.close();
        searcher.close();
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
    
    @Override
    public CloseableIterator<STMT> findStatements(ID subject, UID predicate,
            NODE object, UID context, boolean includeInferred) {
        if (subject != null || predicate != null || object != null || context != null){
            BooleanQueryBuilder query = new BooleanQueryBuilder();
            if (subject != null){
                query.and(new Term(Constants.ID_FIELD_NAME, getID(subject)));
            }   
            if (predicate != null){
                if (object != null){
                    query.and(new Term(predicate.getValue(), conf.getConverter().toString(object)));
                }else{
                    
                }
            }else if (object != null){
                
            }
            
            if (context != null){
                query.and(new Term(Constants.CONTEXT_FIELD_NAME, getContextID(context)));
            }
            
            
            
        }else{
            // wildcard
        }
        
        
        // TODO : lucene find
        return null;
    }
    
    private String getContextID(ID resource) {
        return resource == null ? CONTEXT_NULL : getID(resource);        
    }
    
    private LuceneDocument getDocument(Term term) throws IOException {
        TopDocs docs = searcher.search(new TermQuery(term), 1);
        if (docs.scoreDocs.length > 0){
            return new LuceneDocument(conf, searcher.doc(docs.scoreDocs[0].doc));
        }else{
            return null;
        }
    }
    
    private String getID(ID resource) {
        Assert.notNull(resource);
        if (resource.isBNode()){
            return BNODE_ID_PREFIX + resource.getId();
        }else{
            return resource.getId();
        }
    }
    
    public void search(Query query, Collector results) throws IOException{
        searcher.search(query, results);
    }
    
    private void update(ListMap<ID, STMT> rsAdded, ListMap<ID, STMT> rsRemoved,
            Set<ID> resources) throws IOException, CorruptIndexException {
        // for each resource, add/remove
        for (ID resource : resources) {
            // is the resource in the store?

            // fetch the Document representing this Resource
            String id = getID(resource);
            Term idTerm = new Term(ID_FIELD_NAME, id);
            LuceneDocument document = getDocument(idTerm);

            if (document == null) {
                // there is no such Document: create one now
                document = new LuceneDocument(conf);
                document.addId(id);
                // add all statements, remember the contexts
                HashSet<ID> contextsToAdd = new HashSet<ID>();
                List<STMT> list = rsAdded.get(resource);
                if (list != null){
                    for (STMT s : list) {
                        document.addProperty(s);
                        contextsToAdd.add(s.getContext());
                    }
                }
                    
                if (conf.isContextsStored()){
                    // add all contexts
                    for (ID c : contextsToAdd) {
                        document.addContext(getContextID(c));
                    }                    
                }
                
                // add it to the index
                writer.addDocument(document.getDocument());

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
                LuceneDocument newDocument = new LuceneDocument(conf);

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
                for (Object oldFieldObject : document.getFields()) {
                    Field oldField = (Field) oldFieldObject;
                    // do not copy removed statements to the new version of the
                    // document
                    if (removedOfResource != null) {
                        // which fields were removed?
                        List<String> objectsRemoved = removedOfResource.get(oldField.name());
                        if ((objectsRemoved != null) && (objectsRemoved.contains(oldField.stringValue()))) {
                            continue;
                        }

                    }
                    newDocument.add(oldField);
                }

                // add all statements to this document, remember the contexts
                {
                    List<STMT> addedToResource = rsAdded.get(resource);
                    if (addedToResource != null) {
                        HashSet<ID> contextsToAdd = new HashSet<ID>();
                        for (STMT s : addedToResource) {
                            newDocument.addProperty(s);
                            contextsToAdd.add(s.getContext());
                        }
                        // add all contexts
                        for (ID c : contextsToAdd) {
                            newDocument.addContext(getContextID(c));
                        }
                    }
                }

                // update the index with the cloned document
                writer.updateDocument(idTerm, newDocument.getDocument());
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
