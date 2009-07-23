package com.mysema.rdfbean.sesame.query;

import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.object.SimpleBeanQuery;

/**
 * BeanQueryReferenceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
// FIXME
public abstract class BeanQueryReferenceTest extends BeanQueryStandardTest{
    protected BeanQuery newQuery(){
        return new SimpleBeanQuery(session);
    } 
}
