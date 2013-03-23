/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.RDF;

@ClassMapping
public class MappedPathTest {

    public String getProperty() {
        return null;
    }

    @Test
    public void ToString() throws SecurityException, NoSuchMethodException {
        Configuration configuration = new DefaultConfiguration(TEST.NS, MappedPathTest.class);
        MappedClass mappedClass = configuration.getMappedClass(MappedPathTest.class);
        MethodProperty property = new MethodProperty(MappedPathTest.class.getMethod("getProperty"), mappedClass);
        MappedPredicate predicate = new MappedPredicate(TEST.NS, new DummyPredicate(RDF.type), null);
        MappedPath path = new MappedPath(property, Collections.<MappedPredicate> singletonList(predicate), false);
        assertEquals("public java.lang.String com.mysema.rdfbean.object.MappedPathTest.getProperty() { http://www.w3.org/1999/02/22-rdf-syntax-ns#type }",
                path.toString());
    }

}
