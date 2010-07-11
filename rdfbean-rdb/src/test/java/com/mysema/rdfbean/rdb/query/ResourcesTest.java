package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.object.FlushMode;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.rdb.AbstractRDBTest;

public class ResourcesTest extends AbstractRDBTest{
    
    @ClassMapping(ns=TEST.NS)
    public static class Resource {
        
        @Id(IDType.RESOURCE)
        private ID id;

        public ID getId() {
            return id;
        }
        
    }
    
    private Session session;
    
    @Before
    public void setUp(){
        session = SessionUtil.openSession(repository, Resource.class);
        session.setFlushMode(FlushMode.ALWAYS);        
    }
    
    @After
    public void tearDown() throws IOException{
        if (session != null){
            session.close();
        }
    }
    
    @Test
    public void test(){
        Resource resource = new Resource();
        session.save(resource);
        
        Resource r = Alias.alias(Resource.class);
        List<Resource> resources = session.from($(r)).list($(r)); 
        assertTrue(resources.contains(resource));        
    }

}
