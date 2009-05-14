/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassMapping maps a Java type to an RDF type
 * 
 * @author sasa
 *
 */
@Target( { TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMapping {

	/**
	 * @return The whole URI or the local name of the mapped resource. Uses class or property name as
	 *         default (empty string) if namespace is given.
	 */
	String ln() default "";

	/**
	 * @return Namespace of the resource or empty string, if localName contains
	 *         whole URI.
	 */
	String ns() default "";

}
