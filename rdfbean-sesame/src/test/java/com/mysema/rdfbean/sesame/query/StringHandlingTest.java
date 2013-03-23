/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.StringPath;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ SimpleType.class, SimpleType2.class })
public class StringHandlingTest extends SessionTestBase {

    private StringPath stringPath = var.listProperty.get(1).directProperty;

    private BeanQuery where(Predicate... conditions) {
        return session.from(var).where(conditions);
    }

    @Test
    public void UntypedLiterals() {
        assertEquals(1, where(var.directProperty.eq("metaonto_elements")).count());
    }

    @Test
    public void Matches() {
        assertEquals(1, where(var.directProperty.matches("propertym.*")).count());
        assertEquals(1, where(var.directProperty.matches(".*opertym.*")).count());

        assertEquals(1, where(var.directProperty.startsWith("propertym")).count());
        assertEquals(1, where(var.directProperty.contains("opertym")).count());
    }

    @Test
    public void TypedLiterals() {
        assertEquals(1, where(var.directProperty.eq("propertymap")).count());
    }

    @Test
    public void StartsWith() {
        assertEquals(1, where(stringPath.startsWith("ns")).count());
    }

    @Test
    public void StartsWithIgnoreCase() {
        assertEquals(1, where(stringPath.startsWithIgnoreCase("NS")).count());
    }

    @Test
    public void EndsWith() {
        assertEquals(1, where(stringPath.endsWith("ix")).count());
    }

    @Test
    public void EndsWithIgnoreCase() {
        assertEquals(1, where(stringPath.endsWithIgnoreCase("IX")).count());
    }

    @Test
    public void Contains() {
        assertEquals(2, where(stringPath.contains("i")).count());
    }

    @Test
    @Ignore
    public void In() {
        // Sesame doesn't support RDFS Datatype inferencing rules properly
        assertEquals(1, where(stringPath.in("metaonto_elements", "B", "C")).count());
    }

}
