/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.tapestry.services;

import java.io.IOException;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.EagerLoad;

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
    
    public SeedEntityImpl(SessionFactory sessionFactory, List<Object> entities) throws IOException {
        Session session = sessionFactory.openSession();        
        RDFBeanTransaction tx = session.beginTransaction();        
        try{
            for (Object entity : entities){                    
                if (session.getByExample(entity) == null){
                    session.save(entity);    
                }                
            }
            tx.commit();
        }catch(Throwable e){
            tx.rollback();
        }finally{
            session.close();
        }
        
    }

}
