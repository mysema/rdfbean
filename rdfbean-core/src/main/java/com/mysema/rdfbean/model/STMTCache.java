/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.collections15.iterators.SingletonIterator;

/**
 * @author tiwe
 */
public final class STMTCache {
    
    @Nullable
    private Set<STMT> multi;
    
    @Nullable
    private STMT single;
    
    public STMTCache(@Nullable STMT single) {
        this.single = single;
    }
    
    public void add(STMT stmt) {
        if (multi == null) {
            if (single == null) {
                single = stmt;
            } else if (!stmt.equals(single)) {
                multi = new LinkedHashSet<STMT>();
                multi.add(single);
                multi.add(stmt);
            }
        } else {
            multi.add(stmt);
        }
    }
    
    public Iterator<STMT> iterator() {
        if (multi == null) {
            if (single == null) {
                return Collections.<STMT>emptyList().iterator();
            } else {
                return new SingletonIterator<STMT>(single);
            }
        } else {
            return multi.iterator();
        }
    }
    
    public boolean remove(STMT stmt) {
        if (multi == null) {
            if (stmt.equals(single)) {
                single = null;
                return true;
            } else {
                return false;
            }
        } else {
            return multi.remove(stmt);
        }
    }
    
    public String toString() {
        if (multi != null) {
            return multi.toString();
        } else if (single != null) {
            return single.toString();
        } else {
            return "";
        }
    }
}