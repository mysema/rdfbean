/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.sesame.query;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;

import com.mysema.commons.lang.Assert;

/**
 * JoinBuilder provides
 *
 * @author tiwe
 * @version $Id$
 */
public class JoinBuilder{
    
    private final boolean datatypeInference;
    
    private boolean optional;
    
    private List<StatementPattern> patterns = new ArrayList<StatementPattern>();
    
    private TupleExpr tupleExpr;
    
    private final ValueFactory vf;
    
    public JoinBuilder(ValueFactory vf, boolean datatypeInference){
        this.vf = Assert.notNull(vf);
        this.datatypeInference = datatypeInference; 
    }

    public JoinBuilder add(StatementPattern pattern){
        patterns.add(pattern);        
        return this;
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
    
    public TupleExpr getTupleExpr() {
        if (!patterns.isEmpty()){
            tupleExpr = merge(patterns, tupleExpr);
        }        
        return tupleExpr;
    }
    
    public boolean isEmpty() {
        return tupleExpr == null && patterns.isEmpty();
    }
    
    private TupleExpr merge(List<StatementPattern> patterns, TupleExpr base){
        TupleExpr rv = base;
        for (StatementPattern pattern : patterns){
            if (rv != null){
                rv = new Join(rv, convert(pattern));
            }else{
                rv = convert(pattern);
            }
        }
        patterns.clear();
        return rv;        
    }
    
    public void setMandatory(){
        if (!patterns.isEmpty()){        
            tupleExpr = new LeftJoin(tupleExpr, merge(patterns, null)); 
            patterns.clear();
        }        
        optional = false;
    }    
        
    public void setOptional(){
        if (!patterns.isEmpty()){
            tupleExpr = merge(patterns, tupleExpr);
        }        
        optional = true;
    }
    
}