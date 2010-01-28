package com.mysema.rdfbean.sesame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * SavingTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SavingTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS)
    public static class Revision {
        @Predicate
        long svnRevision;
        
        @Predicate
        long created;
                
        @Predicate
        Entity revisionOf;
                                        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Entity {        
        @Id
        String id;

        @Predicate
        Document document;
        
        @Predicate
        String text;
                                
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Document {        
        @Id
        String id;
        
        @Predicate
        String text;
                   
    }
    
    @Test
    public void test_MemoryRepository() throws StoreException{
        System.out.println("test_MemoryRepository");
        session = createSession(Document.class, Entity.class, Revision.class);
        session.setFlushMode(FlushMode.MANUAL);
        
        loadTest(session, 10);
        loadTest(session, 50);
        loadTest(session, 100);
        loadTest(session, 500);
        loadTest(session, 1000);    
    }
    
    @Test
    public void test_MiniRepository() throws IOException{
        System.out.println("test_MiniRepository");
        Session localSession = SessionUtil.openSession(Document.class, Entity.class, Revision.class);
        
        try{
            loadTest(localSession, 10);
            loadTest(localSession, 50);
            loadTest(localSession, 100);
            loadTest(localSession, 500);
            loadTest(localSession, 1000);    
        }finally{
            localSession.close();
        }        
    }
    
    private void loadTest(Session session, int size){
        session.setFlushMode(FlushMode.MANUAL);
        List<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < size; i++){
            Document document = new Document();
            document.text = UUID.randomUUID().toString();
            objects.add(document);
            
            Entity entity = new Entity();
            entity.document = document;
            entity.text = UUID.randomUUID().toString();
            objects.add(entity);
            
            for (int created : Arrays.asList(1,2,3,4,5,6)){
                Revision rev = new Revision();
                rev.svnRevision = 1;
                rev.revisionOf = entity;
                rev.created = created;
                objects.add(rev);            
            }   
        }
        
        long t1 = System.currentTimeMillis();
        for (Object o : objects){
            session.save(o);
        }
        long t2 = System.currentTimeMillis();
        session.flush();
        long t3 = System.currentTimeMillis();
        System.err.println("Save of " + objects.size() + " objects took " + (t2-t1)+"ms");
        System.err.println("Flush took " + (t3-t2)+"ms");
        System.err.println();
    }

}
