/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ListMap provides
 *
 * @author tiwe
 * @version $Id$
 *
 * @param <K>
 * @param <V>
 */
@Deprecated
public class ListMap<K, V> extends AbstractCollectionMap<K,V>{

    @Override
    public Collection<V> createCollection() {
        return new ArrayList<V>();
    }

    @Override
    public List<V> get(K key) {
        return (List<V>)(data.containsKey(key) ? data.get(key) : Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K,List<V>>> entrySet(){
        return (Set)data.entrySet();
    }

}
