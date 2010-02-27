package com.mysema.util;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

/**
 * @author tiwe
 */
public final class MultiMapFactory {
    
    private MultiMapFactory(){}
    
    public static <K,V> MultiMap<K,V> create(){
        return new MultiHashMap<K,V>();
    }

    @SuppressWarnings("serial")
    public static <K,V> MultiMap<K,V> createWithSet(){
        return new MultiHashMap<K,V>(){
            @Override
            protected Collection<V> createCollection(Collection<? extends V> col) {
                return col == null ? new HashSet<V>() : new HashSet<V>(col);
            }
        };
    }

}
