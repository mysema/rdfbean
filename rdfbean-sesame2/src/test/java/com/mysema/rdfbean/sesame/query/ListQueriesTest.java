/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.NumberExpression;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

/**
 * ListQueriesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@SessionConfig({SimpleType.class, SimpleType2.class})
public class ListQueriesTest extends SessionTestBase{

    private final NumberExpression<Integer> size = var.listProperty.size();

    @Test
    public void Persist(){
        SimpleType simpleType = new SimpleType();
        simpleType.listProperty = Arrays.asList(new SimpleType2(), new SimpleType2());
        session.save(simpleType);
        session.clear();

        SimpleType other = session.getById(simpleType.id, SimpleType.class);
        assertEquals(simpleType.listProperty.size(), other.listProperty.size());
        assertEquals(simpleType.listProperty.get(0).id, other.listProperty.get(0).id);
        assertEquals(simpleType.listProperty.get(1).id, other.listProperty.get(1).id);

    }

    @Test
    public void In(){
        where(var.listProperty.contains(var.listProperty.get(0))).count();
        where(var.listProperty.contains(var.listProperty.get(0)),
                var.listProperty.isNotEmpty()).count();
    }

    @Test
    @Ignore
    public void InList(){
        // TODO
    }

    @Test(expected=UnsupportedOperationException.class)
    @Ignore
    public void listPropertyEq(){
        where(var.listProperty.eq(Collections.<SimpleType2>emptyList())).list(var);
    }

    @Test
    @Ignore
    public void SizeEq(){
        // eq
        assertEquals(0, where(size.eq(1)).count());
        assertEquals(1, where(size.eq(2)).count());
        assertEquals(1, where(size.eq(3)).count());
    }

    @Test
    @Ignore
    public void Goe(){
        // goe
        assertEquals(2, where(size.goe(1)).count());
        assertEquals(2, where(size.goe(2)).count());
        assertEquals(1, where(size.goe(3)).count());
        assertEquals(0, where(size.goe(4)).count());
    }

    @Test
    @Ignore
    public void Gt(){
        // gt
        assertEquals(2, where(size.gt(0)).count());
        assertEquals(2, where(size.gt(1)).count());
        assertEquals(1, where(size.gt(2)).count());
        assertEquals(0, where(size.gt(3)).count());
    }

    @Test
    @Ignore
    public void Loe(){
        // loe
        assertEquals(0, where(size.loe(0)).count());
        assertEquals(0, where(size.loe(1)).count());
        assertEquals(1, where(size.loe(2)).count());
        assertEquals(2, where(size.loe(3)).count());
        assertEquals(2, where(size.loe(4)).count());
    }

    @Test
    @Ignore
    public void Lt(){
        // lt
        assertEquals(0, where(size.lt(1)).count());
        assertEquals(0, where(size.lt(2)).count());
        assertEquals(1, where(size.lt(3)).count());
        assertEquals(2, where(size.lt(4)).count());
        assertEquals(2, where(size.lt(5)).count());
    }

    @Test
    public void IsEmpty(){
        assertEquals(0, where(var.listProperty.isEmpty()).count());
        assertEquals(2, where(var.listProperty.isNotEmpty()).count());
    }

    private BeanQuery where(Predicate... conditions) {
        return session.from(var).where(conditions);
    }


}
