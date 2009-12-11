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

    private final Store store;
    
    private final Index index;
    
    private final boolean textIndexed;
    
    private final boolean allIndexed;
    
    public PropertyConfig(Store store, Index index, boolean textIndexed, boolean allIndexed) {
        this.store = Assert.notNull(store);
        this.index = Assert.notNull(index);
        this.textIndexed = textIndexed;
        this.allIndexed = allIndexed;
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

}
