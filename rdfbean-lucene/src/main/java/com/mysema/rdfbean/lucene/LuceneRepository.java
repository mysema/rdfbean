/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.compass.core.Compass;
import org.compass.core.CompassSession;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.Operation;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.io.Format;

/**
 * LuceneRepository provides
 *
 * @author tiwe
 * @version $Id$
 */
public class LuceneRepository implements Repository{

    private Compass compass;
    
    private LuceneConfiguration configuration;
    
    public LuceneRepository(){}
    
    public LuceneRepository(LuceneConfiguration configuration){
        this.configuration = Assert.notNull(configuration,"configuration");
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
    public <RT> RT execute(Operation<RT> operation) {
        throw new UnsupportedOperationException();        
    }

    @Override
    public void export(Format format, Map<String, String> ns2prefix, OutputStream os) {
        throw new UnsupportedOperationException();        
    }
    
    @Override
    public void export(Format format, OutputStream os) {
       throw new UnsupportedOperationException();        
    }

    @Override
    public void initialize() {
        if (compass == null){
            Assert.notNull(configuration, "configuration has not been set");
            configuration.initialize();
            compass = configuration.getCompass();    
        }else{
            throw new IllegalStateException("Compass has already been initialized!");
        }                
    }
    
    @Override
    public void load(Format format, InputStream is, @Nullable UID context, boolean replace) {
        throw new UnsupportedOperationException();         
    }

    @Override
    public LuceneConnection openConnection() {
        if (compass != null){
            CompassSession compassSession = compass.openSession();
            return new LuceneConnection(configuration, compassSession);
        }else{
            throw new IllegalStateException("Compass has not yet been initialized!");
        }
        
    }

    public void setConfiguration(LuceneConfiguration configuration) {
        this.configuration = configuration;
    }


}
