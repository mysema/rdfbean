/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * DuplicateVarNamesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class DuplicateVarNamesTest extends AbstractSesameQueryTest{

    @Test
    public void test(){
        QTestType v = new QTestType("va");
        assertEquals(2, newQuery().from(v).list(v.directProperty).size());
    }
}
