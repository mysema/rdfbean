/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines ComponentType of a Collection or value type of a Map if it's not reachable through 
 * generic types. 
 * 
 * @author sasa
 */
@Target( { METHOD, FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentType {

	Class<?> value();
	
}
