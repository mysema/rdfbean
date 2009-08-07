/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * &#64;Path mapping allows mapping predicate path's into read-only properties. 
 * A path consist of direct and inverse predicates.
 * 
 * @author sasa
 */
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    /**
     * @return true if invalid values should be ignored. 
     */
    boolean ignoreInvalid() default false;
    
    /**
     * @return Default namespace for predicates.
     */
    String ns() default "";

    /**
     * @return predicate path.
     */
    Predicate[] value();
    
}
