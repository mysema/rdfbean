/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.SortedSet;
import java.util.TreeSet;

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;

/**
 * JoinBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JoinBuilder{
    
    private SortedSet<Pattern> patterns = new TreeSet<Pattern>();

    public TupleExpr getJoins() {
        TupleExpr rv = null;
        for (Pattern pattern : patterns){
            if (rv == null){
                rv = pattern.getPattern();
            }else if (pattern.isOptional()){
                rv = new LeftJoin(rv, pattern.getPattern());
            }else{
                rv = new Join(rv, pattern.getPattern());
            }
        }
        return rv;
    }
    
    public JoinBuilder leftJoin(StatementPattern pattern) {
        patterns.add(new Pattern(pattern, true));
        return this;
    }

    public JoinBuilder join(StatementPattern pattern) {
        patterns.add(new Pattern(pattern));
        return this;
    }
    
    private static class Pattern implements Comparable<Pattern>{
        private StatementPattern pattern;
        private boolean optional;
        public Pattern(StatementPattern pattern){
            this(pattern, false);
        }   
        
        public Pattern(StatementPattern pattern, boolean optional){
            this.pattern = pattern;
            this.optional = optional;
        }        
        @Override
        public int compareTo(Pattern o) {
            int rv = compare(pattern.getSubjectVar(), o.pattern.getSubjectVar());
            if (rv == 0){
                rv = compare(pattern.getPredicateVar(), o.pattern.getPredicateVar());
                if (rv == 0){
                    return compare(pattern.getObjectVar(), o.pattern.getObjectVar());
                }else{
                    return rv;
                }
            }else{
                return rv;
            }
        }
        
        @Override
        public boolean equals(Object o){
            return o instanceof Pattern && ((Pattern)o).pattern.equals(pattern);
        }
        
        @Override
        public int hashCode(){
            return pattern.hashCode();
        }
        
        private int compare(Var var1, Var var2){
            return var1.getName().compareTo(var2.getName());
        }
        
        public StatementPattern getPattern() {
            return pattern;
        }
        public boolean isOptional() {
            return optional;
        }        
    }

    public boolean isEmpty() {
        return patterns.isEmpty();
    }
    
}