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
 * @author sasa
 *
 */
@Target( { METHOD, FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentType {

	Class<?> value();
	
}
