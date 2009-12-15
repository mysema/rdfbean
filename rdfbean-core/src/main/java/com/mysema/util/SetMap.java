package com.mysema.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * SetMap provides
 *
 * @author tiwe
 * @version $Id$
 */
public class SetMap<K,V> {
    
    private final HashMap<K, Set<V>> data;

    public SetMap() {
        data = new HashMap<K, Set<V>>();
    }

    public void put(K key, V value) {
        Set<V> set;
        if (data.containsKey(key)) {
            set = data.get(key);
        } else {
            set = new HashSet<V>();
            data.put(key, set);
        }
        set.add(value);
    }

    public Set<V> get(K key) {
        return data.get(key);
    }

    public boolean containsKey(K key) {
        return data.containsKey(key);
    }

    public String toString() {
        return data.toString();
    }

    public void remove(K key) {
        data.remove(key);
    }

}
