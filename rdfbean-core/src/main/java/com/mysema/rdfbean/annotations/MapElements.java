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
 * &#64;MapElements defines how Map's keys and values are mapped to RDF. Map's
 * are mapped using 1) &#64;Predicate to define target and 2) MapElements to
 * define how that object is mapped to Map elements.
 * 
 * <pre>
 * &#064;Predicate(ns = RDFS.NS, ln = &quot;subClassOf&quot;, inv = true)
 * &#064;MapElements(key = @Predicate(ns = RDFS.NS, ln = &quot;label&quot;))
 * private Map&lt;String, RDFSClass&gt; subClassesByLabel;
 * </pre>
 * 
 * If the component (value) type is not readable from property's generic
 * signature, it can be defined using &#64;ComponentType.
 * 
 * @author sasa
 */
@Documented
@Target({ METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MapElements {

    // TODO: Refactor into Path
    Predicate key();

    Class<?> keyType() default Void.class;

    String ns() default "";

    // TODO: Refactor into Path
    Predicate value() default @Predicate(ns = "");
    
    String context() default "";

}
