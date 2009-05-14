/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sasa
 *
 */
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Default {

    /**
     * @return The whole URI or the local name of the mapped resource. Uses class or property name as
     *         default (empty string) if namespace is given.
     */
    String ln() default "";

    /**
     * @return Namespace of the resource or empty string, if localName contains
     *         whole URI.
     */
    String ns();

}
