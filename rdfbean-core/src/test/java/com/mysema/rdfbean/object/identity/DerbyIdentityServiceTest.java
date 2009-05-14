/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;


/**
 * DerbyIdentityServiceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DerbyIdentityServiceTest {
    
    private String url = "jdbc:derby:target/derbydb;create=true"; 
    
    private DerbyIdentityService identityService;
    
    private UID context = new UID(RDF.NS);
    
    @Before
    public void setUp() throws IOException{        
        identityService = new DerbyIdentityService(url);
    }
    
    @After
    public void tearDown() throws IOException{
        identityService.reset();
    }
    
    @Test
    public void testURI(){     
        LID lid = identityService.getLID(RDF.type);
        assertEquals(RDF.type, identityService.getID(lid));
        assertEquals(RDF.type, identityService.getID(lid));
    }
    
    @Test
    public void testURI2(){
        LID lid = identityService.getLID(RDF.type);
        assertEquals(lid, identityService.getLID(RDF.type, RDF.type));
        assertEquals(lid, identityService.getLID(RDF.Property, RDF.type));
        assertFalse(lid.equals(identityService.getLID(RDF.li)));
    }
    
    @Test
    public void testURINamedContext(){     
        LID lid = identityService.getLID(context, RDF.type);
        assertEquals(RDF.type, identityService.getID(lid));
        assertEquals(RDF.type, identityService.getID(lid));
    }
    
    @Test
    public void testBlankNode(){
        BID id = new BID();        
        LID lid = identityService.getLID(context, id);
        assertEquals(id, identityService.getID(lid));
        assertEquals(id, identityService.getID(lid));
    }
    
    @Test
    public void testBlankNode2(){
        BID context = new BID("_:node1445r1ioqx128");
        BID otherContext = new BID("_:node1445r1ioqx120");
        BID id = new BID("_:node1445r1ioqx129");        
        LID lid = identityService.getLID(context, id);
        assertEquals(id, identityService.getID(lid));
        assertEquals(id, identityService.getID(lid));        
        assertFalse(lid.equals(identityService.getLID(otherContext, id)));
    }
    
    @Test
    public void testGetLID(){
        assertTrue(identityService.getID(new LID("123")) == null);
    }
    
    @Test
    @Ignore
    public void testSynchronousExecuction(){
        // TODO : test for deadlocks
    }
    
    @Test
    public void repetitiveInstantiation() throws IOException{
        new DerbyIdentityService(url);
    }
    
}
