package com.mysema.rdfbean.sesame.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.sesame.SessionTestBase;

/**
 * NullProjectionTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NullProjectionTest extends SessionTestBase{
    
    @ClassMapping(ns=TEST.NS, ln="NullProjectionTest_User")
    public static class User{
        
        @Predicate
        private String firstName;
        
        public User(){}
        
        public User(String firstName){
            this.firstName = firstName;            
        }
        
        public String getFirstName(){
            return firstName;
        }
                
    }        
    
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
