/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.TestConfig;

/**
 * DuplicateVarNamesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@TestConfig({SimpleType.class, SimpleType2.class})
public class DuplicateVarNamesTest extends SessionTestBase{

    private Session session;
    
    @Test
    public void test(){
        QSimpleType v = new QSimpleType("va");
        assertEquals(2, session.from(v).list(v.directProperty).size());
    }
}
