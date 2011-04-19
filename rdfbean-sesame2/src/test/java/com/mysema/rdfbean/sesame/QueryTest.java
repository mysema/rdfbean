package com.mysema.rdfbean.sesame;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.mysema.rdfbean.Helper;
import com.mysema.rdfbean.SesameHelper;
import com.mysema.rdfbean.model.BooleanQueryTest;
import com.mysema.rdfbean.model.GraphQueryTest;
import com.mysema.rdfbean.model.RDFUpdateTest;
import com.mysema.rdfbean.model.TupleQueryTest;

@SuiteClasses({BooleanQueryTest.class,
    GraphQueryTest.class,
    TupleQueryTest.class,
    RDFUpdateTest.class})
@RunWith(Suite.class)
public class QueryTest {
    
    @BeforeClass
    public static void beforeClass(){
        Helper.helper = new SesameHelper();
    }
    
}
