package com.mysema.rdfbean.object;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.RDF;


@ClassMapping(ns=TEST.NS)
public class MappedPathTest {
    
    public String getProperty(){
	return null;
    }

    @Test
    public void testToString() throws SecurityException, NoSuchMethodException {
        Configuration configuration = new DefaultConfiguration(MappedPathTest.class);        
	MappedClass mappedClass = configuration.getMappedClass(MappedPathTest.class);
	MethodProperty property = new MethodProperty(MappedPathTest.class.getMethod("getProperty"), mappedClass);
	MappedPredicate predicate = new MappedPredicate(TEST.NS,new DummyPredicate(RDF.type), null);
	MappedPath path = new MappedPath(property,Collections.<MappedPredicate>singletonList(predicate),false);
	assertEquals("public java.lang.String com.mysema.rdfbean.object.MappedPathTest.getProperty() { rdf:type }", path.toString());
    }

}