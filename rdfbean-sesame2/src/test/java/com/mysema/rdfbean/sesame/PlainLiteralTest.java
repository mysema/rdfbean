/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.testutil.SessionConfig;

/**
 * PlainLiteralTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@SessionConfig({SimpleType.class, SimpleType2.class})
public class PlainLiteralTest extends SessionTestBase{
    
    private final QSimpleType simpleType = QSimpleType.simpleType;
    
    @Test
    public void test() throws IOException{                
        // get instance with plain literal value
        SimpleType instance = session
            .from(simpleType)
            .where(simpleType.directProperty.eq("metaonto_elements"))
            .uniqueResult(simpleType);
        assertNotNull(instance);
        
        // modify value and save it
        instance.setDirectProperty("new value");
        session.save(instance);
        session.flush();
        session.clear();
        
        // reload it
        instance = session
            .from(simpleType)
            .where(simpleType.directProperty.eq("new value"))
            .uniqueResult(simpleType);
        assertNotNull(instance);
        assertEquals("new value", instance.getDirectProperty());
        
    }

}
