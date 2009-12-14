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
 * SearchableComponent declares a Predicate annotated property to be 
 * indexed into Lucene, transitively including the properties defined 
 * in the components type declaration
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableComponent {

    /**
     * The depth of component references allowed.
     */
    int maxDepth() default 1;

}