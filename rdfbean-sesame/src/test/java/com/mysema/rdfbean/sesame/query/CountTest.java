package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * CountTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class CountTest extends AbstractSesameQueryTest{

    @Test
    public void test(){
        assertTrue(newQuery().from(var).count() > 0);        
    }
    
    @Test
    public void countWithLimitAndOffset(){
        assertTrue(newQuery().from(var).limit(0l).offset(0).count() > 0);
    }
}
