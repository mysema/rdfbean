/*
 * Copyright (c) 2010 Mysema Ltd.
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
 * &#64;Localized String properties are directly bound to the best matching
 * literal value using session locale preferences. When updating localized
 * String properties, the primary language is expected.
 * 
 * <pre>
 * &#064;Predicate(ns = RDFS.NS)
 * &#064;Localized
 * private String label;
 * </pre>
 * 
 * Another alternative is to use Locale-to-String map:
 * 
 * <pre>
 * &#064;Predicate(ns = RDFS.NS, ln = &quot;label&quot;)
 * &#064;Localized
 * private Map&lt;Locale, String&gt; labels;
 * </pre>
 * 
 * @author sasa
 */
@Documented
@Target({ METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Localized {

}
