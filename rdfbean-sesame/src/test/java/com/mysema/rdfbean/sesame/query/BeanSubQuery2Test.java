/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * BeanSubQuery2Test provides
 *
 * @author tiwe
 * @version $Id$
 */
public class BeanSubQuery2Test extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public static class Revision {
        @Predicate
        long svnRevision;
        
        @Predicate
        long created;
                
        @Predicate
        Entity revisionOf;

        public long getSvnRevision() {
            return svnRevision;
        }

        public long getCreated() {
            return created;
        }

        public Entity getRevisionOf() {
            return revisionOf;
        }
                                        
    }
    

    @ClassMapping(ns=TEST.NS)
    public static class Entity {
        
        @Id
        String id;

        @Predicate
        Document document;
        
        public String getId() {
            return id;
        }

        public Document getDocument() {
            return document;
        }
                                
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Document {
        
        @Id
        String id;

        public String getId() {
            return id;
        }
                                
    }
    
    private Revision rev1 = Alias.alias(Revision.class,"rev1");
    
    private Revision rev2 = Alias.alias(Revision.class,"rev2");

    @Before
    public void setUp() throws StoreException{
        session = createSession(Document.class, Entity.class, Revision.class);
        
        Document document = new Document();
        session.save(document);
        
        Entity entity = new Entity();
        entity.document = document;
        session.save(entity);
        
        for (int created : Arrays.asList(1,2,3,4,5,6)){
            Revision rev = new Revision();
            rev.svnRevision = 1;
            rev.revisionOf = entity;
            rev.created = created;
            session.save(rev);            
        }
                
        session.clear();        
    }
    
    @Test
    public void subQuery_exists() throws StoreException, IOException{   
        Entity entity = session.findInstances(Entity.class).get(0);
        
        List<Revision> results = session.from($(rev1))
             .where(
                 $(rev1.getRevisionOf()).eq(entity),
                 $(rev1.getSvnRevision()).loe(1),
                 sub($(rev2)).where(
                     $(rev2).ne($(rev1)),
                     $(rev2.getRevisionOf()).eq($(rev1.getRevisionOf())),
                     $(rev2.getSvnRevision()).loe(1),
                     $(rev2.getCreated()).gt($(rev1.getCreated()))).notExists())
             .list($(rev1));
        assertEquals(1, results.size());
        Revision result = results.get(0);
        assertEquals(6, result.getCreated());
                 
    }   

    @Test
    public void subQuery_exists2() throws StoreException, IOException{   
        Document document = session.findInstances(Document.class).get(0);
        
        List<Revision> results = session.from($(rev1))
             .where(
                 $(rev1.getRevisionOf().getDocument()).eq(document),
                 $(rev1.getSvnRevision()).loe(1),
                 sub($(rev2)).where(
                     $(rev2).ne($(rev1)),
                     $(rev2.getRevisionOf()).eq($(rev1.getRevisionOf())),
                     $(rev2.getSvnRevision()).loe(1),
                     $(rev2.getCreated()).gt($(rev1.getCreated()))).notExists())
             .list($(rev1));
        assertEquals(1, results.size());
        Revision result = results.get(0);
        assertEquals(6, result.getCreated());                 
    }
    
    @Test
    public void subQuery_all() throws StoreException, IOException{   
        Document document = session.findInstances(Document.class).get(0);
        
        List<Revision> results = session.from($(rev1))
             .where(
                 $(rev1.getRevisionOf().getDocument()).eq(document),
                 $(rev1.getSvnRevision()).loe(1),
                 $(rev1.getCreated()).goe(
                   sub($(rev2)).where(                           
                     $(rev2.getRevisionOf()).eq($(rev1.getRevisionOf())),
                     $(rev2.getSvnRevision()).loe(1)).unique($(rev2.getCreated()))))
             .list($(rev1));
        assertEquals(1, results.size());
        Revision result = results.get(0);
        assertEquals(6, result.getCreated());                 
    }
        
    private BeanSubQuery sub(PEntity<?> entity){
        return new BeanSubQuery().from(entity);
    }

}
