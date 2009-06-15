/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Path;
import com.mysema.rdfbean.annotations.Predicate;

/**
 * TestType2 provides
 *
 * @author tiwe
 * @version $Id$
 */
@ClassMapping(ns = TEST.NS, ln="TestType2")
public class SimpleType2{
    @Path({@Predicate(ln="testType"), @Predicate(ln="directProperty")}) 
    String pathProperty;                       
    
    @Predicate(ln="directProperty2") 
    String directProperty;

    public String getPathProperty() {
        return pathProperty;
    }

    public String getDirectProperty() {
        return directProperty;
    }        
    
    
    
}