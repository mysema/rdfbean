/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mysema.rdfbean.model.IDType;

/**
 * Identifier property. Identifiers can be of three different types: LOCAL, URI or RESOURCE.
 * &#64;Id is required for all updatable classes. 
 * There can be only one identifier property per class (hierarchy). 
 * Actual type of the identifier property depends on IDType: 
 * <dl>
 * <dt>LOCAL</dt>
 * <dd>String or LID</dd>
 * <dt>URI</dt>
 * <dd>String, ID or UID - expecting that all instances are identified by URI</dd>
 * <dt>RESOURCE</dt>
 * <dd>ID</dd>
 * </dl>
 * 
 * For example
 * <pre>
 *   &#64;Id
 *   private String id;
 * </pre>
 * 
 * @see IDType
 * @author sasa
 */
@Target( { METHOD, FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Id {
	IDType value() default IDType.LOCAL;
}
