package com.mysema.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * SetMap provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SetMap<K,V> extends AbstractCollectionMap<K,V> {
    
    @Override
    public Collection<V> createCollection() {
        return new HashSet<V>();
    }

    @Override
    public Set<V> get(K key) {
        return (Set<V>) data.get(key);
    }

}
