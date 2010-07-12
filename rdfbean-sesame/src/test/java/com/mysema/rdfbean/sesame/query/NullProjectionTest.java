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
import com.mysema.rdfbean.domains.UserProjectionDomain;
import com.mysema.rdfbean.domains.UserProjectionDomain.User;
import com.mysema.rdfbean.sesame.SessionTestBase;
import com.mysema.rdfbean.testutil.TestConfig;

/**
 * NullProjectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
@TestConfig(User.class)
public class NullProjectionTest extends SessionTestBase implements UserProjectionDomain{
    
    @Test
    public void testOrderBy() throws StoreException, IOException{
        session.save(new User());
        User user = Alias.alias(User.class, "user");
        List<String> results = session.from($(user)).list($(user.getFirstName()));
        assertFalse(results.isEmpty());
        assertNull(results.get(0));
        
    }
    
}
