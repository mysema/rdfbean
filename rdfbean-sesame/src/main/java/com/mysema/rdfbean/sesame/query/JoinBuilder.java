/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.SortedSet;
import java.util.TreeSet;

import net.jcip.annotations.Immutable;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.*;

import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.sesame.SesameDialect;

/**
 * JoinBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JoinBuilder{
    
    private final SortedSet<JoinElement> elements = new TreeSet<JoinElement>();
    
    private final SesameDialect dialect;
    
    private final URI xsdString;
    
    public JoinBuilder(SesameDialect dialect){
        this.dialect = dialect;
        xsdString = dialect.getURI(XSD.stringType);
    }

    public TupleExpr getJoins() {
        TupleExpr rv = null;
        for (JoinElement pattern : elements){
            if (rv == null){
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
        Var objVar = pattern.getObjectVar();
        if (objVar.getValue() != null && objVar.getValue() instanceof Literal){
            Literal lit = (Literal) pattern.getObjectVar().getValue();
            if (lit.getDatatype() != null && lit.getDatatype().equals(xsdString)){
                Var obj2 = new Var(objVar.getName()+"_untyped", dialect.getLiteral(lit.getLabel()));
                StatementPattern pattern2 = new StatementPattern(
                        pattern.getScope(), 
                        pattern.getSubjectVar(), 
                        pattern.getPredicateVar(),
                        obj2,
                        pattern.getContextVar());
                return new Union(pattern, pattern2);
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
    private static class JoinElement implements Comparable<JoinElement>{
        
        private final StatementPattern pattern;
        
        private final boolean optional;
        
        public JoinElement(StatementPattern pattern, boolean optional){
            this.pattern = pattern;
            this.optional = optional;
        }        
        @Override
        public int compareTo(JoinElement o) {
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
            return o instanceof JoinElement && ((JoinElement)o).pattern.equals(pattern);
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
}