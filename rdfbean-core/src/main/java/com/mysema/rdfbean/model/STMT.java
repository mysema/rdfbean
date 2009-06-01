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
    
    private Boolean asserted;

    public STMT(ID subject, UID predicate, NODE object) {
        this(subject, predicate, object, null);
    }

    public STMT(ID subject, UID predicate, NODE object, UID context) {
        this(subject, predicate, object, context, true);
    }

    public STMT(ID subject, UID predicate, NODE object, UID context, boolean asserted) {
        this.subject = Assert.notNull(subject);
        this.predicate = Assert.notNull(predicate);
        this.object = Assert.notNull(object);
        this.context = context;
        this.asserted = Boolean.valueOf(asserted);
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
        return NODE.hashCode(subject, predicate, object, context, asserted);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof STMT) {
            STMT other = (STMT) obj;
            return NODE.nullSafeEquals(this.subject, other.subject)
                && NODE.nullSafeEquals(this.predicate, other.predicate)
                && NODE.nullSafeEquals(this.object, other.object)
                && NODE.nullSafeEquals(this.context, other.context)
                && NODE.nullSafeEquals(this.asserted, other.asserted);
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

    public boolean isAsserted() {
        return asserted;
    }
    
}
