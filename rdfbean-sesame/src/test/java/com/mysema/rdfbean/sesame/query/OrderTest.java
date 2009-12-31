/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.openrdf.store.StoreException;

import com.mysema.query.alias.Alias;
import static com.mysema.query.alias.Alias.*;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.object.Session;


/**
 * OrderTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class OrderTest extends AbstractSesameQueryTest{
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
        
        @Predicate
        private String firstName;
        
        public String getFirstName(){
            return firstName;
        }
                
    }        
    
    @Test
    public void simpleOrder(){
        List<SimpleType> asc = newQuery().from(var).orderBy(var.directProperty.asc()).list(var);
        System.out.println();
        
        List<SimpleType> desc = newQuery().from(var).orderBy(var.directProperty.desc()).list(var);
        
        if (asc.equals(desc)){
            System.out.println("asc "+  asc);
            System.out.println("desc " + desc);
        }
        assertFalse(asc.equals(desc));
    }
    
    @Test
    public void testOrderBy() throws StoreException{
        Session session = createSession(User.class);
        session.save(new User());
        
        User user = Alias.alias(User.class, "user");
        assertFalse(session.from($(user))
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
        
        assertFalse(session.from($(user))
                .where($(user.getFirstName()).isNull())
                .orderBy($(user.getFirstName()).asc()).list($(user)).isEmpty());
    }

}
