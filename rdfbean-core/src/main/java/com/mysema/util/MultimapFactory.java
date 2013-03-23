/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author tiwe
 */
public final class MultimapFactory {

    private MultimapFactory() {
    }

    public static <K, V> Multimap<K, V> create() {
        return ArrayListMultimap.create();
    }

    public static <K, V> Multimap<K, V> createWithSet() {
        return HashMultimap.create();
    }

}
