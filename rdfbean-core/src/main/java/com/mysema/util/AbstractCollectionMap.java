package com.mysema.util;

import java.util.Collection;
import java.util.HashMap;

/**
 * AbstractContainerMap provides
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class AbstractCollectionMap<K, V>{
    
    protected final HashMap<K, Collection<V>> data;

    public AbstractCollectionMap() {
        data = new HashMap<K, Collection<V>>();
    }
    
    private Collection<V> getCollection(K key){
        if (data.containsKey(key)){
            return data.get(key);
        }else{
            Collection<V> col = createCollection();
            data.put(key, col);
            return col;
        }
    }
    
    public void put(K key, Collection<V> values) {
        getCollection(key).addAll(values);
    }    

    public void put(K key, V value) {
        getCollection(key).add(value);
    }
    
    public abstract Collection<V> createCollection();

    public abstract Collection<V> get(K key);
    
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
