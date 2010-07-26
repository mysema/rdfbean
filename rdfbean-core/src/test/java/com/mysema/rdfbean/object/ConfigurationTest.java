/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import org.junit.Test;

/**
 * ConfigurationTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ConfigurationTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void invalidPackage(){
        new DefaultConfiguration(ConfigurationTest.class.getPackage());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void invalidClass(){
        new DefaultConfiguration(ConfigurationTest.class);
    }

}
