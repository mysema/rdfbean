/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model.fetch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections15.iterators.IteratorChain;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 */
public final class PredicateCache {

    @Nullable
    private List<STMT> containerProperties;
    
    @Nullable
    private Map<UID, STMTCache> predicates;
    
    public void add(STMT stmt) {
        if (RDF.isContainerMembershipProperty(stmt.getPredicate())) {
            if (containerProperties == null) {
                containerProperties = new ArrayList<STMT>();
            }
            containerProperties.add(stmt);
        } else {
            if (predicates == null) {
                predicates = new LinkedHashMap<UID, STMTCache>();
            }
            STMTCache stmts = predicates.get(stmt.getPredicate());
            if (stmts == null) {
                stmts = new STMTCache(stmt);
                predicates.put(stmt.getPredicate(), stmts);
            } else {
                stmts.add(stmt);
            }
        }
    }
    
    public Iterator<STMT> iterator(@Nullable UID predicate) {
        if (predicate == null) {
            IteratorChain<STMT> iterChain = new IteratorChain<STMT>();
            if (predicates != null) {
                for (STMTCache stmts : predicates.values()) {
                    iterChain.addIterator(stmts.iterator());
                }
            }
            if (containerProperties != null) {
                iterChain.addIterator(containerProperties.iterator());
            }
            return iterChain;
        } else if (RDF.isContainerMembershipProperty(predicate)) {
            if (containerProperties != null) {
                return containerProperties.iterator();
            } 
        } else {
            STMTCache stmts  = predicates.get(predicate);
            if (stmts != null) {
                return stmts.iterator();
            }
        }
        return Collections.<STMT>emptyList().iterator();
    }
    
    public boolean remove(STMT stmt) {
        if (RDF.isContainerMembershipProperty(stmt.getPredicate())) {
            if (containerProperties != null) {
                return containerProperties.remove(stmt);
            }
        } else {
            if (predicates != null) {
                STMTCache stmts = predicates.get(stmt.getPredicate());
                if (stmts != null) {
                    return stmts.remove(stmt);
                }
            }
        }
        return false;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (predicates != null) {
            sb.append(predicates.toString());
        } 
        if (containerProperties != null) {
            sb.append(containerProperties.toString());
        }
        return sb.toString();
    }
}