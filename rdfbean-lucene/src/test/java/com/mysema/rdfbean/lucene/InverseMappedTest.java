package com.mysema.rdfbean.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.DefaultConfiguration;


/**
 * InverseMappedTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class InverseMappedTest extends AbstractConfigurationTest{
    
    @Test
    public void Inheritance(){
        configuration.setCoreConfiguration(new DefaultConfiguration(User.class, Account.class));
        configuration.initialize();
        
        UID userPred = new UID(TEST.NS, "user");
        assertNull(configuration.getPropertyConfig(userPred, Collections.singleton(new UID(TEST.NS,"Account"))));
        
        PropertyConfig config = configuration.getPropertyConfig(userPred, Collections.singleton(new UID(TEST.NS,"User")));
        assertNotNull(config);
        assertEquals(true, config.isInverted());
        
    }
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class User{
        
        // inverse searchable
        @SearchablePredicate
        @Predicate(ln="user", inv=true)
        Account account;
        
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Account{
        
    }


}
