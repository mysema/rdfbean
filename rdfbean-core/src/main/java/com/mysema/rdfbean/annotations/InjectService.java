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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the property the value should be acquired from Session's parent ObjectRepository, 
 * e.g. a service bean managed by Spring. Used in conjunction with {@link Predicate} and/or 
 * {@link Default}
 * this can be used to define a) constant, b) data dependent or c) data dependent 
 * with optional default value service references. 
 * <p>
 * Injection can be used for example for 
 * <ul>
 * <li>Pluggable authorization service used in bean's methods 
 *     to check access rights manually usign {@link InjectService} and {@link Default}.</li>
 * <li>Data dependent UI handlers (e.g. Spring MVC Controllers) with some generic 
 *     default controller that is overridable in data using {@link InjectService}, {@link Default} 
 *     and {@link Predicate}.</li>
 * <li>Manual - and thus well controlled - lazy loading.<li>
 * </ul>
 * 
 * @author sasa
 */
@Documented
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InjectService {
    
}
