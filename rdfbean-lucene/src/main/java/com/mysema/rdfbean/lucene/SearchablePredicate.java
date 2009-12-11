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
     * If there is already an existing id with the same field/property name defined,
     * will override it.
     */
    boolean override() default true;

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
     * The store of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#store()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    Store store() default Store.NO;

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
     * The analyzer of the auto generated {@link SearchableMetaData}. Maps to
     * {@link org.compass.annotations.SearchableMetaData#analyzer()}.
     *
     * <p>The meta-data will NOT be auto generated if the field/property have
     * {@link SearchableMetaData}/{@link SearchableMetaDatas} AND the
     * {@link #name()} is not set.
     */
    String analyzer() default "";


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
     * A null value to use to store in the index when the property has a <code>null</code>
     * value. Defaults to not storing null values if the globabl setting of
     * <code>compass.mapping.nullvalue</code> is not set. If it set, disabling the null
     * value can be done by setting it to {@link org.compass.core.config.CompassEnvironment.NullValue#DISABLE_NULL_VALUE_FOR_MAPPING}
     * value (<code>$disable$</code>).
     */
    String nullValue() default "";
}
