/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.sesame.query.QSimpleType;
import com.mysema.rdfbean.sesame.query.SimpleType;
import com.mysema.rdfbean.sesame.query.SimpleType2;

/**
 * PlainLiteralTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class PlainLiteralTest extends SessionTestBase{
    
    private final QSimpleType simpleType = QSimpleType.simpleType;
    
    @Test
    public void test() throws StoreException, IOException{        
        session = createSession(FI, SimpleType.class, SimpleType2.class);
        
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
        session.close();
        
        // reload it
        session = createSession(FI, SimpleType.class, SimpleType2.class);
        instance = session
            .from(simpleType)
            .where(simpleType.directProperty.eq("new value"))
            .uniqueResult(simpleType);
        assertNotNull(instance);
        assertEquals("new value", instance.getDirectProperty());
        
    }

}
