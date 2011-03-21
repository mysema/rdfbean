/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.schema;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.rdfs.RDFSClass;


public class SchemaGenTest {
    
    @ClassMapping(ns=TEST.NS)
    public static class User{
	
	@Predicate
	String firstName;
	
	@Predicate
	String lastName;
	
	@Predicate
	User superVisor;
	
	@Predicate
	Set<Project> inProject;
	
	@Predicate
	Gender gender;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Project {
	
	@Predicate
	String name;
    }
    
    @ClassMapping(ns=TEST.NS)
    public static enum Gender {
        MALE, FEMALE
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Various {
        
        @Predicate
        String strProp;
        
        @Predicate
        int intProp;
        
        @Predicate
        List<String> stringList;
        
        @Predicate
        List<Various> variousList;
        
        @Predicate
        Set<String> stringSet;
        
        @Predicate
        Set<Various> variousSet;
        
        @Predicate
        Various various;
    }
    
    @Test
    public void test() throws IOException{
	Configuration configuration = new DefaultConfiguration(User.class, Project.class, Gender.class, Various.class);
	MiniRepository repository = new MiniRepository();
	
	SchemaGen schemaGen = new SchemaGen();
	schemaGen.addExportNamespace(TEST.NS);
	schemaGen.setConfiguration(configuration);
	schemaGen.setRepository(repository);
	schemaGen.export();
	
	CloseableIterator<STMT> stmts = repository.findStatements(null, CORE.enumOrdinal, null, null, false);
	try{
	    System.out.println(stmts.next());
	    System.out.println(stmts.next());
	    assertFalse(stmts.hasNext());    
	}finally{
	    stmts.close();
	}
	
	Session session = SessionUtil.openSession(repository, OWLClass.class.getPackage(), RDFSClass.class.getPackage());
	session.findInstances(RDFSClass.class);
	
    }

}
