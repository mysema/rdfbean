/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.schema;

import java.util.Set;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.Repository;
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
    }
    
    @ClassMapping(ns=TEST.NS)
    public static class Project {
	
	@Predicate
	String name;
    }
    
    @Test
    public void test(){
	Configuration configuration = new DefaultConfiguration(User.class, Project.class);
	Repository repository = new MiniRepository();
	
	SchemaGen schemaGen = new SchemaGen();
	schemaGen.addExportNamespace(TEST.NS);
	schemaGen.setConfiguration(configuration);
	schemaGen.setRepository(repository);
	schemaGen.exportConfiguration();
	
    }

}
