/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;


/**
 * DerbyIdentityServiceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DerbyIdentityServiceTest extends AbstractIdentityServiceTest{
    
    private String databaseName = "target/derbydb";
    
    @Before
    public void setUp() throws IOException{        
        identityService = new DerbyIdentityService(databaseName);
    }
    
    @After
    public void tearDown(){
        ((DerbyIdentityService)identityService).shutdown();
    }
         
}
