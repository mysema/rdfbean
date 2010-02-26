/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SetMap provides
 *
 * @author tiwe
 * @version $Id$
 */
@Deprecated
public class SetMap<K,V> extends AbstractCollectionMap<K,V> {
    
    @Override
    public Collection<V> createCollection() {
        return new HashSet<V>();
    }

    @Override
    public Set<V> get(K key) {
        return (Set<V>) (data.containsKey(key) ? data.get(key) : Collections.emptySet()); 
    }
    
    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K,Set<V>>> entrySet(){
        return (Set)data.entrySet();
    }

}
