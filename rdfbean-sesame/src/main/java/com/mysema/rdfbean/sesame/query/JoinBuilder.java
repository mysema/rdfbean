/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;

/**
 * JoinBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JoinBuilder{
    
    private final Set<JoinElement> elements = new LinkedHashSet<JoinElement>();
    
    private final ValueFactory vf;
    
    private final boolean datatypeInference;
    
    public JoinBuilder(ValueFactory vf, boolean datatypeInference){
        this.vf = vf;
        this.datatypeInference = datatypeInference; 
    }

    public TupleExpr getJoins() {
        TupleExpr rv = null;
        for (JoinElement pattern : elements){
            if (rv == null){
                if (pattern.isOptional()) throw new IllegalStateException("First join "+pattern+" can't be optional");
                rv = convert(pattern.getPattern());                
            }else if (pattern.isOptional()){
                rv = new LeftJoin(rv, convert(pattern.getPattern()));
            }else{
                rv = new Join(rv, convert(pattern.getPattern()));
            }
        }
        return rv;
    }
    
    private TupleExpr convert(StatementPattern pattern){
        if (datatypeInference){
            Var objVar = pattern.getObjectVar();
            if (objVar.getValue() != null && objVar.getValue() instanceof Literal){
                Literal lit = (Literal) pattern.getObjectVar().getValue();
                if (lit.getDatatype() != null && lit.getDatatype().equals(XMLSchema.STRING)){
                    Var obj2 = new Var(objVar.getName()+"_untyped", vf.createLiteral(lit.getLabel()));
                    StatementPattern pattern2 = new StatementPattern(
                            pattern.getScope(), 
                            pattern.getSubjectVar(), 
                            pattern.getPredicateVar(),
                            obj2,
                            pattern.getContextVar());
                    return new Union(pattern, pattern2);
                }
            }            
        }        
        return pattern;
    }
    
    public JoinBuilder add(StatementPattern pattern, boolean optional){
        elements.add(new JoinElement(pattern, optional));
        return this;
    }
    
    
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    
    @Immutable 
    private static class JoinElement {
        
        private final StatementPattern pattern;
        
        private final boolean optional;
        
        public JoinElement(StatementPattern pattern, boolean optional){
            this.pattern = pattern;
            this.optional = optional;
        }
        
        @Override
        public boolean equals(Object o){
            return o instanceof JoinElement && ((JoinElement)o).pattern.equals(pattern);
        }
        
        @Override
        public int hashCode(){
            return pattern.hashCode();
        }
        
        public StatementPattern getPattern() {
            return pattern;
        }
        
        public boolean isOptional() {
            return optional;
        }        
        
        public String toString(){
            return pattern.toString();
        }
    }
}