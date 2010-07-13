package com.mysema.rdfbean.rdb;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.domains.LoadDomain;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;

@Ignore
public class LoadTest extends AbstractRDBTest implements LoadDomain{
    
    private StringWriter writer = new StringWriter();

    private Configuration configuration = new DefaultConfiguration(Revision.class, Entity.class, Document.class);
    
    @Test
    public void test() throws IOException{       
                
        for (int size : Arrays.asList(10, 50, 100, 500, 1000, 5000, 10000, 50000)){
            SessionFactoryImpl sessionFactory = new SessionFactoryImpl(Locale.ENGLISH);
            sessionFactory.setConfiguration(configuration);
            sessionFactory.setRepository(repository);
            sessionFactory.initialize();
            
            Session localSession = sessionFactory.openSession();
            try{
                loadTest(localSession, size);
            }finally{
                localSession.close();
                sessionFactory.close();
            }            
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
        
        session.clear();
        session.findInstances(Document.class);
        session.findInstances(Entity.class);
        session.findInstances(Revision.class);
        long t4 = System.currentTimeMillis();
        
        // size, save time, flush time, load time
        writer.write(";"+objects.size()+";"+(t2-t1)+";"+(t3-t2)+";"+(t4-t3)+"\n");         
    }

}
