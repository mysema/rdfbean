/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.fetch;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.ObjectUtils;

import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 *
 */
@Immutable
public final class STMTMatcher {
    
    @Nullable private final ID subject;
    
    @Nullable private final UID predicate;
    
    @Nullable private final NODE object;    
    
    @Nullable private final UID context;
    
    private final boolean includeInferred;
    
    public STMTMatcher(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object, @Nullable UID context,
            boolean includeInferred) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
        this.includeInferred = includeInferred;
    }
    
    @Nullable
    public ID getSubject() {
        return subject;
    }
    
    @Nullable
    public UID getPredicate() {
        return predicate;
    }
    
    @Nullable
    public NODE getObject() {
        return object;
    }
    
    @Nullable
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
            
            // s
            if (!ObjectUtils.equals(subject, other.subject)){
        	return false;
            // p
            }else if (!ObjectUtils.equals(predicate, other.predicate)){
        	return false;
            // o
            }else if (!ObjectUtils.equals(object, other.object)){
        	return false;
            // c
            }else if (!ObjectUtils.equals(context, other.context)){
        	return false;
            }                
            
            if (includeInferred != other.includeInferred){
                return false;
            }                
    
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
