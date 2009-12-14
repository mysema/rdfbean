/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.util;

import com.mysema.commons.lang.Assert;

/**
 * Pair provides
 *
 * @author tiwe
 * @version $Id$
 */
public final class Pair<F,S> {
    
    private final F first;
    
    private final S second;
    
    public Pair(F first, S second){
        this.first = Assert.notNull(first);
        this.second = Assert.notNull(second);
    }
    
    @Override
    public int hashCode(){
        return 31 * first.hashCode() + second.hashCode();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o){
        return o instanceof Pair && ((Pair)o).first.equals(first) && ((Pair)o).second.equals(second);
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
    

}
