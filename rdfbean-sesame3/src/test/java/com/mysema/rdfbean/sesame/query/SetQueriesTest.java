package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertFalse;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ SimpleType.class, SimpleType2.class })
public class SetQueriesTest extends SessionTestBase {

    @Test
    public void IsNotEmpty() {
        assertFalse(session.from(var).where(var.setProperty.isNotEmpty()).list(var).isEmpty());
    }

    @Test
    @Ignore
    public void Any() {
        // FIXME
        assertFalse(session.from(var).where(var.setProperty.any().pathProperty.isNotNull()).list(var).isEmpty());
    }

}
