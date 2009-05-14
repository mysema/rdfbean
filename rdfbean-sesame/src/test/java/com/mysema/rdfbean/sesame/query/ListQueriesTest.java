/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.grammar.types.Expr.ENumber;

/**
 * ListQueriesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ListQueriesTest extends AbstractSesameQueryTest{
    
    private ENumber<Integer> size = var.listProperty.size();
    
    @Test
    @Ignore
    public void inList(){
        // TODO
    }

    @Test
    public void eq(){
        // eq
        assertEquals(0, where(size.eq(1)).list(var).size());
        assertEquals(1, where(size.eq(2)).list(var).size());
        assertEquals(1, where(size.eq(3)).list(var).size());
    }
    
    @Test
    public void goe(){
        // goe
        assertEquals(2, where(size.goe(1)).list(var).size());
        assertEquals(2, where(size.goe(2)).list(var).size());
        assertEquals(1, where(size.goe(3)).list(var).size());
        assertEquals(0, where(size.goe(4)).list(var).size());
    }
    
    @Test
    public void gt(){
        // gt
        assertEquals(2, where(size.gt(0)).list(var).size());
        assertEquals(2, where(size.gt(1)).list(var).size());
        assertEquals(1, where(size.gt(2)).list(var).size());
        assertEquals(0, where(size.gt(3)).list(var).size());
    }
    
    @Test
    public void loe(){
        // loe
        assertEquals(0, where(size.loe(0)).list(var).size());
        assertEquals(0, where(size.loe(1)).list(var).size());
        assertEquals(1, where(size.loe(2)).list(var).size());
        assertEquals(2, where(size.loe(3)).list(var).size());
        assertEquals(2, where(size.loe(4)).list(var).size());
    }
    
    @Test
    public void lt(){
        // lt
        assertEquals(0, where(size.lt(1)).list(var).size());
        assertEquals(0, where(size.lt(2)).list(var).size());
        assertEquals(1, where(size.lt(3)).list(var).size());
        assertEquals(2, where(size.lt(4)).list(var).size());
        assertEquals(2, where(size.lt(5)).list(var).size());
    }
    
}
