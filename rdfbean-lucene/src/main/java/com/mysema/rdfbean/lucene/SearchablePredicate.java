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

import org.compass.annotations.SearchableMetaData;
import org.compass.annotations.SearchableMetaDatas;
import org.compass.core.Property.Index;
import org.compass.core.Property.Store;

/**
 * SearchablePredicate provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchablePredicate {
    
    /**
     * Index value in not analyzed form int 'all' field 
     * 
     * @return
     */
    boolean all() default false;
    
    /**
     * The analyzer of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#analyzer()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    String analyzer() default "";

    /**
     * The boost of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#boost()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    float boost() default 1.0f;

    /**
     * The format of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#format()}.
     * The meta-data will be auto generated only if the name has a value.
     *
     * <p>This format will also be used for an internal meta-data id (if required to be
     * generated).
     */
    String format() default "";

    /**
     * The index of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#index()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Index index() default Index.ANALYZED;

    /**
     * The store of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#store()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Store store() default Store.NO;


    /**
     * Index value in analyzed form into 'text' field
     * 
     * @return
     */
    boolean text() default false;

}
