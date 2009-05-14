/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Target context for 
 * @author sasa
 *
 */
@Target( { TYPE, PACKAGE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Context {

    String value();
    
}
