package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.domains.ResourceDomain;
import com.mysema.rdfbean.domains.ResourceDomain.Resource;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.rdb.RDBConnection;
import com.mysema.rdfbean.testutil.TestConfig;

@TestConfig(Resource.class)
public class ResourcesTest extends AbstractRDBTest implements ResourceDomain{
        
    private Session session;
    
    private Resource r = Alias.alias(Resource.class);
    
    private int count;
    
    private Resource resource;
    
    private RDBConnection connection;
    
    @Before
    public void setUp(){
        count = session.from($(r)).list($(r)).size();        
        resource = new Resource();
        session.save(resource);    
    }
    
    @After
    public void tearDown() throws IOException{
        if (connection != null){
            connection.close();
        }
    }
    
    @Test
    public void from_Resource_list(){
        List<Resource> resources = session.from($(r)).list($(r)); 
        assertEquals(count + 1, resources.size());
        assertTrue(resources.contains(resource));        
    }
    
    @Test
    public void rdfType_count_via_Repository(){
        connection = repository.openConnection();
        List<STMT> typeStmts = connection.find(null, RDF.type, new UID(TEST.NS,Resource.class.getSimpleName()), null, false);
        assertEquals(count + 1, typeStmts.size());
        for (STMT typeStmt : typeStmts){            
            assertEquals(1, connection.find(typeStmt.getSubject(), RDF.type, null, null, false).size());
        }                   
    }

    @Test
    public void findInstances(){
        assertEquals(count + 1, session.findInstances(Resource.class).size());
    }
}
