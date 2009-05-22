/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.PACKAGE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mysema.rdfbean.object.Configuration;

/**
 * Package annotation for {@link Configuration} that defines package's mapped classes.
 * 
 * @author sasa
 */
@Target( PACKAGE )
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedClasses {

    Class<?>[] value();
    
}
