package com.mysema.rdfbean.sesame;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.types.path.PathBuilder;


/**
 * SesameSessionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SesameSessionTest extends SessionTestBase {
    
    @Before
    public void setUp() throws StoreException{
        session = createSession(new Class<?>[0]);
    }
    
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
