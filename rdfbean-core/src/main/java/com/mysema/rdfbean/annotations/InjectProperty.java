/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InjectProperty maps constructor parameters into mapped properties. This is very 
 * usable for strict modeling of immutable classes. There's no need for default constructor
 * as all required properties can be provided as constructor parameters.
 * <p>
 * For example:
 * <pre>
 * &#64;ClassMapping(ns=TEST.NS)
 * public final class DateInterval {
 * 
 *   &#64;Prediate
 *   private LocalDate start;
 *   
 *   &#64;Predicate
 *   private LocalDate end;
 *   
 *   public DateInterval(
 *          &#64;InjectProperty("start") 
 *          LocalDate start,
 *          
 *          &#64;InjectProperty("end") 
 *          LocalDate end
 *       ) {
 *     this.start = Assert.notNull(start);
 *     this.end = Assert.notNull(end);
 *   }
 *   
 *   public LocalDate getStart() { return start; }
 *   
 *   public LocalDate getEnd() { return end; }
 * }
 * </pre>
 * NOTE: 
 * <ul>
 * <li>There may be only on mapped constructor per class and</li> 
 * <li>all parameters of that constructor need to be mapped.</li>
 * <li>For read-only properties, one can also use directly {@link Predicate} annotated constructor parameters.</li>
 * 
 * @author sasa
 */
@Documented
@Target( PARAMETER )
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InjectProperty {

    /**
     * @return     name of the property mapped to this constructor parameter.
     */
    String value();
    
}
