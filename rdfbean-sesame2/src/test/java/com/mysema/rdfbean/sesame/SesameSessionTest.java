package com.mysema.rdfbean.sesame;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.testutil.SessionConfig;


/**
 * SesameSessionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@SessionConfig({})
public class SesameSessionTest extends SessionTestBase {
    
    @Test(expected=IllegalArgumentException.class)
    public void findUnknown(){
//        launchpad bug : #576846
        session.findInstances(SesameSessionTest.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void findUnknown2(){
//        launchpad bug : #576846
        PathBuilder<SesameSessionTest> entity = new PathBuilder<SesameSessionTest>(SesameSessionTest.class, "var");
        session.from(entity).list(entity);
    }


}
