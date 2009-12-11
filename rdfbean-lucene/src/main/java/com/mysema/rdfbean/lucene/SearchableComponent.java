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
     * The reference alias that points to the searchable class (either defined using
     * annotations or xml). Not required since most of the times it can be automatically
     * detected.
     */
    String refAlias() default "";

    /**
     * Should the component definitions override an already existing component definitions
     * for the same field/property.
     */
    boolean override() default true;

    /**
     * The depth of cyclic component references allowed.
     */
    int maxDepth() default 1;

    /**
     * An optional prefix that will be appended to all the component referenced class mappings.
     */
    String prefix() default "";

    /**
     * The conveter lookup name that will convert the {@link org.compass.core.mapping.osem.ComponentMapping}.
     * Defaults to compass own intenral {@link org.compass.core.converter.mapping.osem.ComponentMappingConverter}.
     */
    String converter() default "";

    /**
     * The property accessor that will be fetch and write the property value.
     * <p/>
     * It is automatically set based on where the annotation is used, but can be
     * explicitly set. Compass also supports custom property accessors, registered
     * under a custom name, which can then be used here as well.
     */
    String accessor() default "";
}