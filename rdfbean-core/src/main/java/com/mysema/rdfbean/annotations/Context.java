/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mysema.util.NotEmpty;

/**
 * Target context (URI) for given class or classes of annotated package. 
 * <p>
 * Many RDF persistence solutions store quadruples containing the context/model/source of the 
 * statement instead of simple triples. Queries can thus be targeted to specified context. 
 * 
 * @author sasa
 *
 */
@Documented
@Target( { TYPE, PACKAGE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Context {

    @NotEmpty String value();
    
}
