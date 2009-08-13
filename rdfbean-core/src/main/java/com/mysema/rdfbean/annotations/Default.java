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
 * Allows defining default value for object/reference properties. For example a default Role of a User: 
 * <pre>
 * &#64;ClassMapping(ns=TEST.NS)
 * public class User {
 * 
 *   &#64;Default(ns=TEST.NS, "UserRole")
 *   &#64;Predicate
 *   private Role role;
 *   ...
 * }
 * </pre>
 * &#64;Default can also be used as such without &#64;Predicate in order to define non changeable 
 * references. This is particulary useful in conjunction with &#64;Inject for 
 * injecting named services to beans: 
 * <pre>
 *   &#64;Default(ns=TEST.SERVICES_NS, "userService")
 *   &#64;Inject
 *   private UserService userService;
 * </pre>
 * 
 * @author sasa
 */
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Default {

    /**
     * @return The local name of the resource URI. If namespace (ns) is not given, this is should be the full URI. 
     */
    String ln() default "";

    /**
     * @return Namespace of the resource or empty string, if localName contains
     *         whole URI.
     */
    String ns();

}
