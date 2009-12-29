/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.types.expr.ENumber;

/**
 * ListQueriesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ListQueriesTest extends AbstractSesameQueryTest{
    
    private ENumber<Integer> size = var.listProperty.size();
    
    @Test
    public void in(){
        where(var.listProperty.contains(var.listProperty.get(0))).count();
        where(var.listProperty.contains(var.listProperty.get(0)),
              var.listProperty.isNotEmpty()).count();
    }
    
    @Test
    @Ignore
    public void inList(){
        // TODO
    }

    @Test
    public void eq(){
        // eq
        assertEquals(0, where(size.eq(1)).count());
        assertEquals(1, where(size.eq(2)).count());
        assertEquals(1, where(size.eq(3)).count());
    }
    
    @Test
    public void goe(){
        // goe
        assertEquals(2, where(size.goe(1)).count());
        assertEquals(2, where(size.goe(2)).count());
        assertEquals(1, where(size.goe(3)).count());
        assertEquals(0, where(size.goe(4)).count());
    }
    
    @Test
    public void gt(){
        // gt
        assertEquals(2, where(size.gt(0)).count());
        assertEquals(2, where(size.gt(1)).count());
        assertEquals(1, where(size.gt(2)).count());
        assertEquals(0, where(size.gt(3)).count());
    }
    
    @Test
    public void loe(){
        // loe
        assertEquals(0, where(size.loe(0)).count());
        assertEquals(0, where(size.loe(1)).count());
        assertEquals(1, where(size.loe(2)).count());
        assertEquals(2, where(size.loe(3)).count());
        assertEquals(2, where(size.loe(4)).count());
    }
    
    @Test
    public void lt(){
        // lt
        assertEquals(0, where(size.lt(1)).count());
        assertEquals(0, where(size.lt(2)).count());
        assertEquals(1, where(size.lt(3)).count());
        assertEquals(2, where(size.lt(4)).count());
        assertEquals(2, where(size.lt(5)).count());
    }
    
    @Test
    public void isEmpty(){
        assertEquals(0, where(var.listProperty.isEmpty()).count());
        assertEquals(2, where(var.listProperty.isNotEmpty()).count());
    }
    
}
