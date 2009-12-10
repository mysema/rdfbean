/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import org.compass.core.Compass;
import org.compass.core.CompassSession;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Repository;

/**
 * LuceneRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepository implements Repository{

    private LuceneConfiguration configuration;
    
    private Compass compass;
    
    public LuceneRepository(){}
    
    public LuceneRepository(LuceneConfiguration configuration){
        this.configuration = Assert.notNull(configuration);
    }
    
    @Override
    public void close() {
        if (compass != null){
            compass.close();    
        }else{
            throw new IllegalStateException("Compass has not yet been initialized!");
        }
    }

    @Override
    public void initialize() {
        if (compass == null){
            Assert.notNull(configuration, "configuration has not been set");
            configuration.initialize();
            compass = configuration.getCompassConfig().buildCompass();    
        }else{
            throw new IllegalStateException("Compass has already been initialized!");
        }                
    }

    @Override
    public LuceneConnection openConnection() {
        if (compass != null){
            CompassSession compassSession = compass.openSession();
            return new LuceneConnection(configuration, compass, compassSession);    
        }else{
            throw new IllegalStateException("Compass has not yet been initialized!");
        }
        
    }

    public void setConfiguration(LuceneConfiguration configuration) {
        this.configuration = configuration;
    }
    
}
