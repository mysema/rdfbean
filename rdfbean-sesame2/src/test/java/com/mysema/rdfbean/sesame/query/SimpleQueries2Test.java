package com.mysema.rdfbean.sesame.query;

import org.junit.BeforeClass;

import com.mysema.rdfbean.domains.SimpleDomain.SimpleType;
import com.mysema.rdfbean.domains.SimpleDomain.SimpleType2;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ SimpleType.class, SimpleType2.class })
public class SimpleQueries2Test extends SimpleQueriesTest {

    private static boolean initialized = false;

    @BeforeClass
    public static void setUpClass() {
        if (!initialized) {
            repository.setSerializeQueries(true);
            initialized = true;
        }
    }

}
