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
 * Searchable provides
 *
 * @author tiwe
 * @version $Id$
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    /**
     * The alias that is associated with the class. Can be used to refernce
     * the searchable class when performing search operations, or for other
     * mappings to extend it.
     * <p/>
     * Default value is the short name of the class.
     */
    String alias() default "";

    /**
     * The sub index the searchable class will be saved to. A sub index is
     * a fully functional index.
     *
     * <p>When joining several searchalbe classes into the same index,
     * the search will be much faster, but updates perform locks on the sub index
     * level, so it might slow it down.
     *
     * <p>Defaults to the searchable class {@link #alias()} value.
     *
     * <p>More fine grained control can be used with {@link org.compass.annotations.SearchableSubIndexHash}.
     */
    String subIndex() default "";

    /**
     * Boost level for the searchable class. Controls the ranking of hits
     * when performing searches.
     */
    float boost() default 1.0f;

    /**
     * Defines if the searchable class is a root class. A root class is a top
     * level searchable class. You should define the searchable class with <code>false</code>
     * if it only acts as mapping definitions for a {@link SearchableComponent}.
     */
    boolean root() default true;

    /**
     * A specialized analyzer (different from the default one) associated with the
     * searchable class. Note, that this will associate the class statically with
     * an analyzer. Dynamically associating the class with an analyzer can be done
     * by using the {@link SearchableAnalyzerProperty} to use the property value
     * to dynamically lookup the value for the analyzer to use.
     */
    String analyzer() default "";

}