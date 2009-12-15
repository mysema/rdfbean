/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import net.jcip.annotations.Immutable;

import org.compass.core.Property.Index;
import org.compass.core.Property.Store;

/**
 * PropertyConfig provides
 *
 * @author tiwe
 * @version $Id$
 */
@Immutable
public class PropertyConfig {

    /**
     * Index value in not analyzed form into 'all' field 
     * 
     */
    private final boolean allIndexed;
    
    /**
     * 
     * 
     */
    private final boolean inverted;
    
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
    
    public PropertyConfig(Store store, Index index, boolean textIndexed, boolean allIndexed, boolean inverted, float boost) {
        this.store = store != null ? store : Store.NO;
        this.index = index != null ? index : Index.NO;
        this.textIndexed = textIndexed;
        this.allIndexed = allIndexed;
        this.inverted = inverted;
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

    public boolean isInverted() {
        return inverted;
    }
    
    
    
}
