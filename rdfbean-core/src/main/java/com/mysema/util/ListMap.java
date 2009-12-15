/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListMap<K, V> {

    private final HashMap<K, List<V>> data;

    public ListMap() {
        data = new HashMap<K, List<V>>();
    }

    public void put(K key, V value) {
        List<V> list;
        if (data.containsKey(key)) {
            list = data.get(key);
        } else {
            list = new ArrayList<V>();
            data.put(key, list);
        }
        list.add(value);
    }

    public List<V> get(K key) {
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
