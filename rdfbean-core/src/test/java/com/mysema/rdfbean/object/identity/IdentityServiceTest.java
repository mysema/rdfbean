/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object.identity;

import java.io.IOException;

import org.junit.Before;


/**
 * DerbyIdentityServiceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IdentityServiceTest extends AbstractIdentityServiceTest{
    
    @Before
    public void setUp() throws IOException{        
        identityService = MemoryIdentityService.instance();
    }
        
}
