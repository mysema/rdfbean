/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.schema;

import static org.junit.Assert.*;

import java.io.IOException;
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
    
    @Test
    public void test() throws IOException{
	Configuration configuration = new DefaultConfiguration(User.class, Project.class, Gender.class);
	MiniRepository repository = new MiniRepository();
	
	SchemaGen schemaGen = new SchemaGen();
	schemaGen.addExportNamespace(TEST.NS);
	schemaGen.setConfiguration(configuration);
	schemaGen.setRepository(repository);
	schemaGen.exportConfiguration();
	
	CloseableIterator<STMT> stmts = repository.findStatements(null, CORE.enumOrdinal, null, null, false);
	try{
	    System.out.println(stmts.next());
	    System.out.println(stmts.next());
	    assertFalse(stmts.hasNext());    
	}finally{
	    stmts.close();
	}
	
    }

}
