/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

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

    @SuppressWarnings("unchecked")
    public Iterator<STMT> iterator(@Nullable UID predicate) {
        if (predicate == null) {
            List<Iterator<STMT>> iterators = Lists.newArrayList();
            if (predicates != null) {
                for (STMTCache stmts : predicates.values()) {
                    iterators.add(stmts.iterator());
                }
            }
            if (containerProperties != null) {
                iterators.add(containerProperties.iterator());
            }
            return Iterators.concat(iterators.toArray(new Iterator[iterators.size()]));
        } else if (RDF.isContainerMembershipProperty(predicate)) {
            if (containerProperties != null) {
                return containerProperties.iterator();
            }
        } else {
            STMTCache stmts = predicates.get(predicate);
            if (stmts != null) {
                return stmts.iterator();
            }
        }
        return Collections.<STMT> emptyList().iterator();
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