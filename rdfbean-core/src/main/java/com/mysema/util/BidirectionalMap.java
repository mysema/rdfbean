/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.collections15.Factory;

import com.mysema.commons.lang.Assert;

/**
 * @author sasa
 *
 */
public class BidirectionalMap<K, V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = -5192883231277011164L;

    @Nullable
    private Factory<V> factory;
    
    private Map<K, V> values; 
    
    private Map<V, K> keys;
    
    public BidirectionalMap() {
        this(new LinkedHashMap<K, V>(), new HashMap<V, K>(), null);
    }
    
    public BidirectionalMap(Factory<V> factory) {
        this(new LinkedHashMap<K, V>(), new HashMap<V, K>(), factory);
    }
    
    public BidirectionalMap(Map<K, V> values) {
        this(values, new HashMap<V, K>(), null);
    }
    
    public BidirectionalMap(Map<K, V> values, Factory<V> factory) {
        this(values, new HashMap<V, K>(), factory);
    }
    
    private BidirectionalMap(Map<K, V> values, Map<V, K> keys, @Nullable Factory<V> factory) {
        this.values = Assert.notNull(values);
        this.keys = Assert.notNull(keys);
        this.factory = factory;
        putAll(values);
    }
    
    public void clear() {
        values.clear();
        keys.clear();
    }

    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return keys.containsKey(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return values.entrySet();
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BidirectionalMap) {
            return this.values.equals(((BidirectionalMap) o).values);
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public @Nullable V get(Object key) {
        if (!values.containsKey(key)) {
            if (factory != null) {
                V value = factory.create();
                values.put((K) key, value);
                keys.put(value, (K) key);
                return value;
            } else {
                return null;
            }
        } else {
            return values.get(key);
        }
    }

    public @Nullable K getKey(V value) {
        return keys.get(value);
    }

    public int hashCode() {
        return values.hashCode();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Set<K> keySet() {
        return values.keySet();
    }

    @Override
    public @Nullable V put(K key, V value) {
        keys.put(value, key);
        return values.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        values.putAll(m);
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            keys.put(entry.getValue(), entry.getKey());
        }
    }

    public V remove(Object key) {
        V value = values.remove(key);
        if (value != null) {
            keys.remove(value);
        }
        return value;
    }

    public Map<K, V> getValues() {
        return values;
    }
    
    public Factory<V> getFactory() {
        return factory;
    }
    
    public int size() {
        return values.size();
    }

    public Collection<V> values() {
        return values.values();
    }
}
