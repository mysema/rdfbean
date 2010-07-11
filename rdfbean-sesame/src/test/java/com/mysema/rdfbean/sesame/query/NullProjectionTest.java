/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.NullProjectionDomain;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * NullProjectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NullProjectionTest extends SessionTestBase implements NullProjectionDomain{
    
    @Test
    public void testOrderBy() throws StoreException, IOException{
        Session session = createSession(User.class);
        try{
            session.save(new User());
            User user = Alias.alias(User.class, "user");
            List<String> results = session.from($(user)).list($(user.getFirstName()));
            assertFalse(results.isEmpty());
            assertNull(results.get(0));
        }finally{
            session.close();
        }
        
    }
    
}
