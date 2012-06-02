/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.ResourceDomain;
import com.mysema.rdfbean.domains.ResourceDomain.Resource;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(Resource.class)
public class ResourcesTest extends AbstractRDBTest implements ResourceDomain{
        
    private Resource r = Alias.alias(Resource.class);
    
    private int count;
    
    private Resource resource;
    
    @Before
    public void setUp(){
        count = session.from($(r)).list($(r)).size();        
        resource = new Resource();
        session.save(resource);    
    }
    
    @Test
    public void From_Resource_list(){
        List<Resource> resources = session.from($(r)).list($(r)); 
        assertEquals(count + 1, resources.size());
        assertTrue(resources.contains(resource));        
    }
    
    @Test
    public void FindInstances(){
        assertEquals(count + 1, session.findInstances(Resource.class).size());
    }
}
