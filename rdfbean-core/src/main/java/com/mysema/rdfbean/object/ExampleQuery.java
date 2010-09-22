/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BeanMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.commons.lang.Assert;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import com.mysema.rdfbean.model.BID;

/**
 * ExampleQuery provides support query by example queries
 *
 * @author tiwe
 * @version $Id$
 */
public class ExampleQuery <T>{    

    private static final PathBuilderFactory pathBuilderFactory = new PathBuilderFactory();
    
    private static final Set<Class<?>> DATE_TIME_TYPES = new HashSet<Class<?>>(Arrays.<Class<?>>asList(
            LocalDate.class,
            LocalTime.class,
            DateTime.class, 
            java.util.Date.class,
            java.sql.Date.class,
            java.sql.Time.class,
            java.sql.Timestamp.class
            ));
    
    private final Session session;
    
    private final PathBuilder<T> entityPath;
    
    private final BooleanBuilder conditions;
    
    /**
     * Create a new Query-by-example query
     * 
     * @param session
     * @param entity
     */
    @SuppressWarnings("unchecked")
    public ExampleQuery(Configuration configuration, Session session, T entity){
        this.session = Assert.notNull(session,"session");
        this.entityPath = (PathBuilder) pathBuilderFactory.create(entity.getClass());
        this.conditions = new BooleanBuilder();
        BeanMap beanMap = new BeanMap(entity);        
        MappedClass mappedClass = configuration.getMappedClass(entity.getClass());        
        for (MappedPath mappedPath : mappedClass.getProperties()){
            MappedProperty<?> property = mappedPath.getMappedProperty();
            Object value = property.getValue(beanMap);
            if (value != null 
                 // date/time values are skipped
                 && !DATE_TIME_TYPES.contains(value.getClass())
                 // collection values are skipped
                 && !property.isCollection()
                 // map values are skipped
                 && !property.isMap()
                 // blank nodes are skipped
                 && !(value instanceof BID)){
                Expression<Object> propertyPath = (Expression)entityPath.get(property.getName(), property.getType());
                conditions.and(ExpressionUtils.eqConst(propertyPath, value));
            }
        }
    }

    @Nullable
    public T uniqueResult() {
        if (conditions.getValue() != null){
            return session.from(entityPath).where(conditions).uniqueResult(entityPath);
        }else{
            return null;    
        }        
    }
    
    public List<T> list(){
        if (conditions.getValue() != null){
            return session.from(entityPath).where(conditions).list(entityPath);
        }else{
            return Collections.emptyList();
        }
    }

}
