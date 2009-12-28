/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.util.ArrayList;
import java.util.Collection;
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
public class ListMap<K, V> extends AbstractCollectionMap<K,V>{

    @Override
    public Collection<V> createCollection() {
        return new ArrayList<V>();
    }

    @Override
    public List<V> get(K key) {
        return (List<V>) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K,List<V>>> entrySet(){
        return (Set)data.entrySet();
    }

}
