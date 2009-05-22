/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * &#64;Mixin defines that a property's value is another instance of the same subject. 
 * Thus it is never used in conjunction with &#64;Predicate. &#64;Mixin property's type
 * may not be assignable from the host type (self reference). 
 * <p>
 * Mixin references are useful when there's multiple projections (DTO's) for the same 
 * underlying resource, e.g. one containing only basic information about the subject and 
 * another containing everything else. The class containing details don't have to repeat
 * info classes properties as it can reuse it using &#64;Mixin property.
 * </p>
 * <p>
 * For example 
 * <pre>
 * &#64;ClassMapping(ns=OWL.NS, ln="Class")
 * public class ClassInfo {
 *   
 *   &#64;Id
 *   private UID id;
 *   
 *   &#64;Predicate(ns=RDFS.NS)
 *   private String label;
 * }
 * 
 * &#64;ClassMapping(ns=OWL.NS, ln="Class")
 * public class ClassDetails {
 * 
 *   &#64;Predicate(ns=RDFS.NS)
 *   private Set&lt;ClassInfo> subClassOf;
 *   
 *   &#64;Mixin
 *   private ClassInfo info;
 *   
 *   &#64;Predicate
 *   private Set&lt;OWLClass> complementOf = new LinkedHashSet&lt;OWLClass>();
 *   
 *   &#64;Predicate
 *   private Set&lt;OWLClass> disjointWith = new LinkedHashSet&lt;OWLClass>();
 *   
 *   ...
 * }
 * </pre>
 * 
 * 
 * @author sasa
 */
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Mixin {

}
