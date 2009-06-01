/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import com.mysema.commons.lang.Assert;
import com.mysema.query.annotations.Entity;


/**
 * @author sasa
 *
 */
@Entity
public final class STMT {
    
    private ID subject;
    
    private UID predicate;
    
    private NODE object;
    
    private UID context;

    public STMT(ID subject, UID predicate, NODE object) {
        this.subject = Assert.notNull(subject);
        this.predicate = Assert.notNull(predicate);
        this.object = Assert.notNull(object);
    }

    public STMT(ID subject, UID predicate, NODE object, UID context) {
        this.subject = Assert.notNull(subject);
        this.predicate = Assert.notNull(predicate);
        this.object = Assert.notNull(object);
        this.context = context;
    }

    public NODE getObject() {
        return object;
    }

    public UID getPredicate() {
        return predicate;
    }

    public ID getSubject() {
        return subject;
    }
    
    public int hashCode() {
        return NODE.hashCode(subject, predicate, object);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof STMT) {
            STMT other = (STMT) obj;
            return NODE.nullSafeEquals(this.subject, other.subject)
                && NODE.nullSafeEquals(this.predicate, other.predicate)
                && NODE.nullSafeEquals(this.object, other.object)
                && NODE.nullSafeEquals(this.context, other.context);
        } else {
            return false;
        }
    }
    
    public String toString() {
        return "" + subject + " " + predicate + " " + object;
    }

    public UID getContext() {
        return context;
    }

    public boolean isObjectStatement() {
        return object instanceof ID;
    }
    
}
