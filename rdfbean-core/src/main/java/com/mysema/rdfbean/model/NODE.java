/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.Serializable;

import com.mysema.query.annotations.Entity;

/**
 * @author sasa
 *
 */
@Entity
public abstract class NODE implements Serializable {

    private static final long serialVersionUID = -6921484648846884179L;

    // This is closed api
    NODE() {}
    
    public abstract String getValue();
    
    static boolean nullSafeEquals(Object n1, Object n2) {
        if (n1 == null) {
            if (n2 == null) {
                return true;
            } else {
                return false;
            }
        } else if (n2 == null) {
            return false;
        } else {
            return n1.equals(n2);
        }
    }
    
    public abstract NodeType getNodeType();

    static int hashCode(Object... objects) {
        int hashCode = 1;
        for (Object obj : objects) {
            hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
        }
        return hashCode;
    }

    public abstract boolean isResource();
    
    public abstract boolean isURI();
    
    public abstract boolean isBNode();

    public abstract boolean isLiteral();
    
}
