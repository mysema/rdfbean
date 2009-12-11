/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * SearchableComponent provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableComponent {

    /**
     * The depth of cyclic component references allowed.
     */
    int maxDepth() default 1;

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ComponentMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ComponentMappingConverter}.
     */
    String converter() default "";

}