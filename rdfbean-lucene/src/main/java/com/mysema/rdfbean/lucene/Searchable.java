/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */

package com.mysema.rdfbean.lucene;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Searchable provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {
    
    /**
     * Store all predicates defined in this projection, 
     * doesn't cover predicate mappings of subtypes
     * 
     * @return
     */
    boolean storeAll() default false;
    
    /**
     * Boost level for the searchable class. Controls the ranking of hits
     * when performing searches.
     */
    float boost() default 1.0f;

    /**
     * A specialized analyzer (different from the default one) associated with the
     * searchable class. Note, that this will associate the class statically with
     * an analyzer. Dynamically associating the class with an analyzer can be done
     * by using the {@link SearchableAnalyzerProperty} to use the property value
     * to dynamically lookup the value for the analyzer to use.
     */
    String analyzer() default "";

}