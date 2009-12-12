/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import net.jcip.annotations.Immutable;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;

import com.mysema.commons.lang.Assert;

/**
 * PropertyConfig provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class PropertyConfig {

    /**
     * Index value in not analyzed form int 'all' field 
     * 
     */
    private final boolean allIndexed;
    
    /**
     * 
     */
    private final float boost;
    
    /**
     * 
     */
    private final Index index;
    
    /**
     * 
     */
    private final Store store;
    
    /**
     * Index value in analyzed form into 'text' field
     * 
     */
    private final boolean textIndexed;
    
    public PropertyConfig(Store store, Index index, boolean textIndexed, boolean allIndexed, float boost) {
        this.store = Assert.notNull(store);
        this.index = Assert.notNull(index);
        this.textIndexed = textIndexed;
        this.allIndexed = allIndexed;
        this.boost = boost;
    }

    public float getBoost() {
        return boost;
    }

    public Index getIndex() {
        return index;
    }

    public Store getStore() {
        return store;
    }

    public boolean isAllIndexed() {
        return allIndexed;
    }

    public boolean isTextIndexed() {
        return textIndexed;
    }
    
}