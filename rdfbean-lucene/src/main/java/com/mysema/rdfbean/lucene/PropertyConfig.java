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
     * 
     */
    private final Store store;
    
    /**
     * 
     */
    private final Index index;
    
    /**
     * Index value in analyzed form into 'text' field
     * 
     */
    private final boolean textIndexed;
    
    /**
     * Index value in not analyzed form int 'all' field 
     * 
     */
    private final boolean allIndexed;
    
    /**
     * 
     */
    private final float boost;
    
    public PropertyConfig(Store store, Index index, boolean textIndexed, boolean allIndexed, float boost) {
        this.store = Assert.notNull(store);
        this.index = Assert.notNull(index);
        this.textIndexed = textIndexed;
        this.allIndexed = allIndexed;
        this.boost = boost;
    }

    public Store getStore() {
        return store;
    }

    public Index getIndex() {
        return index;
    }

    public boolean isTextIndexed() {
        return textIndexed;
    }

    public boolean isAllIndexed() {
        return allIndexed;
    }

    public float getBoost() {
        return boost;
    }
    
}
