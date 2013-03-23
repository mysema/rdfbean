package com.mysema.rdfbean.jena;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.mysema.rdfbean.Helper;
import com.mysema.rdfbean.JenaHelper;
import com.mysema.rdfbean.model.BooleanQueryTest;
import com.mysema.rdfbean.model.GraphQueryTest;
import com.mysema.rdfbean.model.RDFUpdateTest;
import com.mysema.rdfbean.model.TupleQueryTest;

@Ignore
@SuiteClasses({ BooleanQueryTest.class,
        GraphQueryTest.class,
        TupleQueryTest.class,
        RDFUpdateTest.class })
@RunWith(Suite.class)
public class QueryTest {

    @BeforeClass
    public static void beforeClass() {
        Helper.helper = new JenaHelper();
    }

}
