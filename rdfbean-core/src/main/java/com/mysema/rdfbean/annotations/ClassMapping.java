/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassMapping maps a Java type to an RDF type. 
 * <p>
 * For example http://example.org/domain#Person can be mapped as 
 * <pre>
 * &#64;ClassMapping(ns="http://example.org/domain#")
 * public class Person {
 * ...
 * }
 * </pre>
 * or 
 * <pre>
 * &#64;ClassMapping(ln="http://example.org/domain#Person")
 * public class PersonEntity {
 * ...
 * }
 * </pre>
 * or
 * <pre>
 * &#64;ClassMapping(ns="http://example.org/domain#", ln="Person")
 * public class User {
 * ...
 * }
 * </pre>
 * 
 * It is highly recommended to use constants for namespaces. E.g. &#64;ClassMapping(ns=TestConstants.NS).
 * 
 * </p>
 * @author sasa
 *
 */
@Documented
@Target( { TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMapping {

    /**
     * @return The local name of the mapped resource. Defaults to class's simple name.
     *         If namespace (ns) is not given, this is should be the full URI. 
     */
    String ln() default "";

    /**
     * @return Namespace of the resource or empty, if localName contains
     *         the whole URI.
     */
    String ns() default "";

}
