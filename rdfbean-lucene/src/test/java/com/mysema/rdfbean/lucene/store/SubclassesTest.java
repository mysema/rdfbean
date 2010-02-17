/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.lucene.Constants;
import com.mysema.rdfbean.lucene.Searchable;
import com.mysema.rdfbean.lucene.SearchableText;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

/**
 * IndexingTypeHierarchiesTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SubclassesTest extends AbstractStoreTest{
    
    @Test
    @Ignore
    public void bankAccount() throws IOException{
        // FIXME
        BankAccount account = new BankAccount();
        account.accountNumber = "123";
        testSaveAndQuery(session, account);
    }
    
    @Test
    @Ignore
    public void creditAcount() throws IOException{
        // FIXME
        CreditAccount account = new CreditAccount();
        account.accountNumber = "456";
        testSaveAndQuery(session, account);
                
    }
    
    @Override
    protected Configuration getCoreConfiguration() {
        return new DefaultConfiguration(Account.class, BankAccount.class, CreditAccount.class);
    }

    public void setUp() throws IOException, InterruptedException{
        super.setUp();
        session = SessionUtil.openSession(repository, Account.class, BankAccount.class, CreditAccount.class);
    }
      
    private void testSaveAndQuery(Session session, Account account){
        session.save(account);
        assertNotNull("id was not assigned", account.id);
        session.clear();
        
        // get by type
        assertNotNull("get by type failed", session.getById(account.id, account.getClass()));
        session.clear();
        
        // get by supertype
        assertNotNull("get by supertype failed", session.getById(account.id, Account.class));
        session.clear();
        
        // find instances by type
        assertFalse("find instances by type failed", session.findInstances(account.getClass()).isEmpty());
        session.clear();
        
        // find instances by supertype
        assertFalse("find instances by supertype failed", session.findInstances(Account.class).isEmpty());
        session.clear();
        
        // query by type
        assertFalse("query by type failed", 
                session.createQuery(Constants.LUCENEQUERY).query(account.accountNumber).list(account.getClass()).isEmpty());
        session.clear();
        
        // query by supertype
        assertFalse("query by supertype failed", 
                session.createQuery(Constants.LUCENEQUERY).query(account.accountNumber).list(Account.class).isEmpty());
        session.clear();
    }
    
    @Searchable
    @ClassMapping(ns=TEST.NS)
    public static class Account{
        @SearchableText
        @Predicate
        String accountNumber;
    
        @Id
        String id;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class BankAccount extends Account{
        
    }


    @ClassMapping(ns=TEST.NS)
    public static class CreditAccount extends Account{
        
    }
}
