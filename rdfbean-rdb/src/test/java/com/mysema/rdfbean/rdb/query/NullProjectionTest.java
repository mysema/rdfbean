package com.mysema.rdfbean.rdb.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.UserProjectionDomain;
import com.mysema.rdfbean.domains.UserProjectionDomain.User;
import com.mysema.rdfbean.rdb.AbstractRDBTest;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig(User.class)
public class NullProjectionTest extends AbstractRDBTest implements UserProjectionDomain{
    
    @Test
    public void testOrderBy() throws IOException{
        session.save(new User());
        User user = Alias.alias(User.class, "user");
        List<String> results = session.from($(user)).list($(user.getFirstName()));
        assertFalse(results.isEmpty());
        assertNull(results.get(0));        
    }    
    
}
