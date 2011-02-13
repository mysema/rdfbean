/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.MiniConnection;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;

public class IdentityServiceTest {

    private IdentityService identityService;
    
    @Before
    public void setUp(){
        MiniRepository repository = new MiniRepository();
        RDFConnection connection = new MiniConnection(repository);
        identityService = new SessionIdentityService(connection);
    }
    
    @Test
    public void URI(){     
        LID lid = identityService.getLID(RDF.type);
        assertEquals(RDF.type, identityService.getID(lid));
        assertEquals(RDF.type, identityService.getID(lid));
    }
    
    @Test
    public void URI2(){
        LID lid = identityService.getLID(RDF.type);
        assertEquals(lid, identityService.getLID(RDF.type));
        assertEquals(lid, identityService.getLID(RDF.type));
        assertFalse(lid.equals(identityService.getLID(RDF.li)));
    }
    
    @Test
    public void URINamedContext(){     
        LID lid = identityService.getLID(RDF.type);
        assertEquals(RDF.type, identityService.getID(lid));
        assertEquals(RDF.type, identityService.getID(lid));
    }
    
    @Test
    public void BlankNode(){
        BID id = new BID();        
        LID lid = identityService.getLID(id);
        assertEquals(id, identityService.getID(lid));
        assertEquals(id, identityService.getID(lid));
    }
    
    @Test
    public void BlankNode2(){
        BID id = new BID("_:node1445r1ioqx129");        
        LID lid = identityService.getLID(id);
        assertEquals(id, identityService.getID(lid));
        assertEquals(id, identityService.getID(lid));       
        assertEquals(lid, identityService.getLID(id));
    }
    
//    @Test(expected=IllegalArgumentException.class)
//    public void getID(){
//        identityService.getID(new LID(String.valueOf(Long.MAX_VALUE)));
//    }
    
}
