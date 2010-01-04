/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BeanMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.mysema.commons.lang.Assert;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.expr.Expr;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.PathBuilderFactory;
import com.mysema.rdfbean.model.BID;

/**
 * ExampleQuery provides
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
    
    @SuppressWarnings("unchecked")
    public ExampleQuery(Session session, T entity){
        this.session = Assert.notNull(session);
        this.entityPath = (PathBuilder) pathBuilderFactory.create(entity.getClass());
        this.conditions = new BooleanBuilder();
        BeanMap beanMap = new BeanMap(entity);        
        MappedClass mappedClass = MappedClass.getMappedClass(entity.getClass());        
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
                Expr<Object> propertyPath = (Expr)entityPath.get(property.getName(), property.getType());
                conditions.and(propertyPath.eq(value));
            }
        }
    }

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
