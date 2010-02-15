/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

/**
 * @author sasa
 *
 */
@Immutable
public final class STMTMatcher {
    
    @Nullable private final ID subject;
    
    @Nullable private final UID predicate;
    
    @Nullable private final NODE object;    
    
    private final UID context;
    
    private final boolean includeInferred;
    
    public STMTMatcher(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, UID context,
            boolean includeInferred) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
        this.includeInferred = includeInferred;
    }
    public ID getSubject() {
        return subject;
    }
    public UID getPredicate() {
        return predicate;
    }
    public NODE getObject() {
        return object;
    }
    public UID getContext() {
        return context;
    }
    public boolean isIncludeInferred() {
        return includeInferred;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + (includeInferred ? 1231 : 1237);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof STMTMatcher) {
            STMTMatcher other = (STMTMatcher) obj;
            
            if (subject == null) {
                if (other.subject != null)
                    return false;
            } else if (!subject.equals(other.subject))
                return false;

            if (predicate == null) {
                if (other.predicate != null)
                    return false;
            } else if (!predicate.equals(other.predicate))
                return false;

            if (object == null) {
                if (other.object != null)
                    return false;
            } else if (!object.equals(other.object))
                return false;

            if (context == null) {
                if (other.context != null)
                    return false;
            } else if (!context.equals(other.context))
                return false;
            
            if (includeInferred != other.includeInferred)
                return false;
    
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean matches(STMT stmt, ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return 
        // Subject match
        (subject == null || subject.equals(stmt.getSubject())) &&

        // Predicate match
        (predicate == null || predicate.equals(stmt.getPredicate())) &&
        
        // Object match
        (object == null || object.equals(stmt.getObject())) &&
        
        // Context match
        (context == null || context.equals(stmt.getContext())) &&
        
        // Asserted or includeInferred statement
        (includeInferred || stmt.isAsserted());
    }
    
    public boolean matches(STMT stmt) {
        return matches(stmt, subject, predicate, object, context, includeInferred);
    }
}
