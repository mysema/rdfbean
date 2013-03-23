/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.model;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class STMTMatcherTest {

    @Test
    public void EqualsObject() {
        STMTMatcher matcher1 = new STMTMatcher(null, null, null, null, true);
        STMTMatcher matcher2 = new STMTMatcher(RDF.type, null, null, null, true);
        STMTMatcher matcher3 = new STMTMatcher(null, RDF.type, null, null, true);
        STMTMatcher matcher4 = new STMTMatcher(null, null, RDF.type, null, true);
        STMTMatcher matcher5 = new STMTMatcher(null, null, null, RDF.type, true);

        matcher1.hashCode();
        matcher2.hashCode();
        matcher3.hashCode();
        matcher4.hashCode();
        matcher5.hashCode();

        assertFalse(matcher1.equals(matcher2));
        assertFalse(matcher2.equals(matcher3));
        assertFalse(matcher3.equals(matcher4));
        assertFalse(matcher4.equals(matcher5));
    }

}
