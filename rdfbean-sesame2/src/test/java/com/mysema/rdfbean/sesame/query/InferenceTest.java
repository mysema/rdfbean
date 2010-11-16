/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.InferenceDomain;
import com.mysema.rdfbean.domains.InferenceDomain.Entity1;
import com.mysema.rdfbean.domains.InferenceDomain.Entity2;
import com.mysema.rdfbean.domains.InferenceDomain.Entity3;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.SessionConfig;

/**
 * InferenceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@SessionConfig({Entity1.class, Entity2.class, Entity3.class})
public class InferenceTest extends SessionTestBase implements InferenceDomain{
    
    @Test
    @Ignore // FIXME
    public void SubClassOf() throws StoreException{
        session.save(new Entity1());
        session.save(new Entity2());
        session.save(new Entity3());
        session.flush();
        
        // query via session
        assertEquals(3, session.findInstances(Entity1.class).size());
        assertEquals(2, session.findInstances(Entity2.class).size());
        assertEquals(1, session.findInstances(Entity3.class).size());
        
        // query via BeanQuery
        Entity1 var1 = Alias.alias(Entity1.class);
        Entity2 var2 = Alias.alias(Entity2.class);
        Entity3 var3 = Alias.alias(Entity3.class);        
        assertEquals(3, session.from($(var1)).list($(var1)).size());
        assertEquals(2, session.from($(var2)).list($(var2)).size());
        assertEquals(1, session.from($(var3)).list($(var3)).size());
    }
    
    // TODO : subPropertyOf

}
