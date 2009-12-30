/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.BeanMap;
import org.apache.tapestry5.ioc.annotations.EagerLoad;

import com.mysema.query.types.path.PEntity;
import com.mysema.query.types.path.PSimple;
import com.mysema.query.types.path.PathMetadata;
import com.mysema.rdfbean.annotations.InverseFunctionalProperty;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.RDFBeanTransaction;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * SeedEntityImpl provides
 *
 * @author tiwe
 * @version $Id$
 */
@EagerLoad
public class SeedEntityImpl implements SeedEntity{
    
    private Map<MappedClass,MappedProperty<?>> inverseFunctionalProperties = new HashMap<MappedClass,MappedProperty<?>>();
    
    public SeedEntityImpl(SessionFactory sessionFactory, List<Object> entities) throws IOException {
        Session session = sessionFactory.openSession();        
        RDFBeanTransaction tx = session.beginTransaction();        
        try{
            for (Object entity : entities){
                MappedClass mappedClass = MappedClass.getMappedClass(entity.getClass());
                MappedProperty<?> property = getInverseFunctionalProperty(mappedClass);
                if (property != null){
                    PEntity<Object> entityPath = new PEntity<Object>(entity.getClass(), 
                            entity.getClass().getSimpleName(), 
                            PathMetadata.forVariable("entity"));
                    PSimple<Object> propertyPath = new PSimple<Object>(property.getType(), 
                            entityPath, 
                            property.getName());
                    Object propertyValue = property.getValue(new BeanMap(entity));
                    if (propertyValue != null){
                        Object savedEntity = session.from(entityPath)
                            .where(propertyPath.eq(propertyValue))
                            .uniqueResult(entityPath);
                        if (savedEntity != null){
                            continue;
                        }    
                    }                    
                }
                session.save(entity);
            }
            tx.commit();
        }catch(Throwable e){
            tx.rollback();
        }finally{
            session.close();
        }
        
    }

    private MappedProperty<?> getInverseFunctionalProperty(MappedClass mappedClass) {
        if (inverseFunctionalProperties.containsKey(mappedClass)){
            return inverseFunctionalProperties.get(mappedClass);
        }else{
            MappedProperty<?> property = null;
            for (MappedPath path : mappedClass.getProperties()){
                if (path.getMappedProperty().getAnnotation(InverseFunctionalProperty.class) != null){
                    property = path.getMappedProperty();
                    break;
                }
            }    
            inverseFunctionalProperties.put(mappedClass, property);
            return property;
        }        
    }

}
