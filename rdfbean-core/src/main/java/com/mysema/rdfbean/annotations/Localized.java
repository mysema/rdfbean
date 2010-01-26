/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * &#64;Localized String properties are directly bound to the best matching literal value 
 * using session locale preferences. When updating localized String properties, the primary 
 * language is expected. 
 * <pre>
 *   &#64;Predicate(ns=RDFS.NS)
 *   &#64;Localized
 *   private String label;
 * </pre>
 * Another alternative is to use Locale-to-String map: 
 * <pre>
 *   &#64;Predicate(ns=RDFS.NS, ln="label")
 *   &#64;Localized
 *   private Map<Locale, String> labels;
 * </pre>
 * 
 * @author sasa
 */
@Documented
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Localized {

}
