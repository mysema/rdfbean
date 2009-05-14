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
 * @author sasa
 * 
 */
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {

    boolean includeInferred() default false;
    
    boolean ignoreInvalid() default false;

    /**
     * @return true if property is mapped to inverse of this predicate, i.e.
     *         (*value*, predicate, this). If false, then maps directly to this
     *         predicate (this, predicate, *value*).
     */
    boolean inv() default false;

    /**
     * @return Local name of the mapped resource. Uses class or property name as
     *         default (empty string).
     */
    String ln() default "";

    /**
     * @return Namespace of the resource. If empty string, uses parent namespace
     */
    String ns() default "";

    String context() default "";
    
}
