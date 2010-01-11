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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
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
    
    private static final Logger logger = LoggerFactory.getLogger(SeedEntityImpl.class);
    
    private final Map<Object,Object> persisted;
    
    public SeedEntityImpl(SessionFactory sessionFactory, List<Object> entities) throws IOException {
        this.persisted = new HashMap<Object,Object>(entities.size());
        Session session = sessionFactory.openSession();        
        RDFBeanTransaction tx = session.beginTransaction();        
        try{
            for (Object entity : entities){
                replaceReferences(entity);
                Object original = session.getByExample(entity);
                if (original == null){
                    session.save(entity);    
                }else{
                    persisted.put(entity, original);
                }
            }
            tx.commit();
        }catch(Throwable e){
            logger.error(e.getMessage(), e);
            tx.rollback();
        }finally{
            session.close();
        }
        
    }

    /**
     * Replace the references of the given entity with persisted ones
     * 
     * @param entity
     */
    private void replaceReferences(Object entity) {
        BeanMap beanMap = new BeanMap(entity);
        MappedClass mappedClass = MappedClass.getMappedClass(entity.getClass());
        for (MappedPath mappedPath : mappedClass.getProperties()){
            Object value = mappedPath.getMappedProperty().getValue(beanMap);
            if (value != null && persisted.containsKey(value)){
                value = persisted.get(value);
                mappedPath.getMappedProperty().setValue(beanMap, value);
            }
        }
    }

}
