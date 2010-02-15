/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class ClassConverter implements Converter<Class<?>>{

    private static final Logger logger = LoggerFactory.getLogger(ClassConverter.class);
    
    @Override
    public Class<?> fromString(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    @Override
    public String toString(Class<?> cl) {
        return cl.getName();
    }

}
