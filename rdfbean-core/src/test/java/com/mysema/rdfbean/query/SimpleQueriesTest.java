/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.testutil.SessionConfig;

@Ignore
@SessionConfig({ SimpleType.class, SimpleType2.class })
public class SimpleQueriesTest extends SessionTestBase {

    protected static final QSimpleType var = new QSimpleType("var");

    protected static final QSimpleType2 var2 = new QSimpleType2("var2");

    private SimpleType instance;

    private List<SimpleType> instances;

    private BeanQuery where(Predicate... conditions) {
        return session.from(var).where(conditions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void in_empty() {
        where(var.directProperty.in(Collections.<String> emptySet())).list(var);
    }

    @Test
    public void AllIds() {
        System.out.println("allIds");
        instances = session.from(var).list(var);
        List<String> ids = Arrays.asList(instances.get(0).getId(), instances.get(1).getId());
        assertEquals(ids, session.from(var).list(var.id));
    }

    @Test
    public void AllInstances() {
        System.out.println("allInstances");
        instances = session.from(var).list(var);
        assertEquals(2, instances.size());
        for (SimpleType i : instances) {
            System.out.println(i.getId() + ", " + i.getDirectProperty());
        }
    }

    @Test
    public void AllDistinctInstances() {
        System.out.println("allDistinctInstances");
        assertEquals(2, session.from(var).listDistinct(var).size());
    }

    @Test
    public void ById() {
        System.out.println("byId");
        String id = session.from(var).list(var.id).get(0);
        instance = where(var.id.eq(id)).uniqueResult(var);
        assertNotNull(instance);
        assertEquals(id, instance.getId());
    }

    @Test
    public void ByIdNegated() {
        System.out.println("byIdNegated");
        instances = session.from(var).list(var);
        instance = where(var.id.ne(instances.get(0).getId())).uniqueResult(var);
        assertNotNull(instance);
        assertEquals(instances.get(1).getId(), instance.getId());
    }

    @Test
    public void ByLiteralProperty() {
        System.out.println("byLiteralProperty");
        instance = where(var.directProperty.eq("propertymap")).uniqueResult(var);
        assertNotNull(instance);
        assertEquals("propertymap", instance.getDirectProperty());
    }

    @Test
    public void ByNonExistantProperty() {
        System.out.println("byNonExistantProperty");
        assertEquals(2, where(var.notExistantProperty.isNull()).list(var).size());
        assertEquals(0, where(var.notExistantProperty.isNotNull()).list(var).size());
    }

    @Test
    public void ByNumericProperty() {
        assertEquals(1, where(var.numericProperty.eq(10)).list(var).size());
        assertEquals(1, where(var.numericProperty.eq(20)).list(var).size());
        assertEquals(0, where(var.numericProperty.eq(30)).list(var).size());

    }

    @Test
    @Ignore
    public void ByReferenceProperty() {
        // TODO
    }

    @Test
    public void IdAndDirectProperties() {
        System.out.println("idAndDirectProperties");
        session.from(var).list(var.id, var.directProperty);
    }

    @Test
    public void TypeOf() {
        System.out.println("typeOf");
        assertEquals(2, where(var.instanceOf(SimpleType.class)).list(var).size());
    }

    @Test
    public void ListAccess() {
        System.out.println("listAccess");
        assertEquals(1, where(var.listProperty.get(1).directProperty.eq("nsprefix")).list(var).size());
    }

    @Test
    public void ListAccess2() {
        System.out.println("listAccess2");
        assertEquals(1, where(var.listProperty.get(0).directProperty.eq("target_idspace")).list(var).size());
    }

    @Test
    @Ignore
    public void MapAccess() {
        // TODO
    }

    @Test
    @Ignore
    public void DtoProjection() {
        // TODO
    }

    @Test
    public void MatchLocale() {
        System.out.println("matchLocale");
        assertEquals(1, where(var.localizedProperty.eq("fi")).list(var).size());
        assertEquals(1, where(var.localizedProperty.eq("en")).list(var).size());
    }

    @Test
    @Ignore
    public void MatchLocale2() {
        // FIXME
        System.out.println("matchLocale2");
        assertEquals(1, where(var.localizedProperty.ne("fi")).list(var).size());
        assertEquals(1, where(var.localizedProperty.ne("en")).list(var).size());
    }

    @Test
    public void LocalizedMap() {
        System.out.println("localizedMap");
        assertEquals(1, where(var.localizedAsMap.get(new Locale("fi")).eq("fi")).list(var).size());
        assertEquals(1, where(var.localizedAsMap.get(new Locale("en")).eq("fi")).list(var).size());
        assertEquals(0, where(var.localizedAsMap.get(new Locale("")).eq("fi")).list(var).size());
    }

    @Test
    public void LocalizedMap2() {
        System.out.println("localizedMap2");
        assertEquals(1, where(var.localizedAsMap.get(new Locale("en")).eq("en")).list(var).size());
        assertEquals(1, where(var.localizedAsMap.get(new Locale("fi")).eq("en")).list(var).size());
        assertEquals(0, where(var.localizedAsMap.get(new Locale("")).eq("en")).list(var).size());
    }

}
