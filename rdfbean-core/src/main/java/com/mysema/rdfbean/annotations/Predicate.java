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
 * Basic mapping of RDF predicates to Java Bean properties. 
 * Annotation may be applied on fields, getters, setters and constructor parameters. 
 * 
 * @author sasa
 */
@Documented
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {

    /**
     * True if inferred statements should be included. For example including 
     * inferred statements of rdfs:subClassOf predicate would return all super
     * classes. 
     * <p>
     * NOTE: Supported inferences depend on actual RDF repository implementation.
     */
    boolean includeInferred() default false;
    
    /**
     * True if invalid values should be ignored. 
     */
    boolean ignoreInvalid() default false;

    /**
     * True if property is mapped to inverse of this predicate, i.e.
     * triple(*value*, predicate, this). If false, then maps directly to this
     * predicate triple(this, predicate, *value*).
     */
    boolean inv() default false;

    /**
     * Local name of the mapped resource. Uses property's name as
     *         default.
     */
    String ln() default "";

    /**
     * Namespace of the predicate. If empty, uses parent namespace (path's or class's).
     */
    String ns() default "";

    /**
     * Context (URI) in which mapped statements reside. For example if 
     * instances and ontology reside in different contexts and one needs
     * meta information about the instance directly through the instance itself:
     * 
     * <pre>
     *   &#64;Path(
     *      &#64;Predicate(ns=RDF.NS, ln="type"), 
     *      &#64;Predicate(ns=RDFS.NS, ln="label", context=ONTOLOGY_CONTEXT)
     *   )
     *   &#64;Localized
     *   private String typeLabel;
     * </pre>
     */
    String context() default "";
    
}
