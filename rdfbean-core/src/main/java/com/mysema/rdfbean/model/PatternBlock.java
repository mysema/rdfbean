package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

import org.apache.commons.lang.ObjectUtils;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class PatternBlock implements Block{
    
    private static final long serialVersionUID = -3450122105441266114L;
    
    private final Expression<ID> subject;
    
    private final Expression<UID> predicate;
    
    private final Expression<NODE> object;
    
    @Nullable
    private final Expression<UID> context;
    
    public PatternBlock(Expression<ID> subject, Expression<UID> predicate, Expression<NODE> object, @Nullable Expression<UID> context) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
    }
    
    public PatternBlock(Expression<ID> subject, Expression<UID> predicate, Expression<NODE> object) {
        this(subject, predicate, object, null);
    }
    
    public Expression<ID> getSubject() {
        return subject;
    }

    public Expression<UID> getPredicate() {
        return predicate;
    }

    public Expression<NODE> getObject() {
        return object;
    }

    @Nullable
    public Expression<UID> getContext() {
        return context;
    }

    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, C> R accept(Visitor<R, C> v, C context) {
        if (v instanceof RDFVisitor){
            return (R)((RDFVisitor)v).visit(this, context);    
        }else if (v instanceof ToStringVisitor){    
            return (R)toString();
        }else{
            throw new IllegalArgumentException(v.toString());
        }
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }
    
    @Override
    public Predicate exists(){
        return new PredicateOperation(Ops.EXISTS, Blocks.group(this));
    }
        
    @Override
    public int hashCode(){
        return subject.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if (o instanceof PatternBlock){
            PatternBlock p = (PatternBlock)o;
            return p.subject.equals(subject)
                && p.predicate.equals(predicate)
                && p.object.equals(object)
                && ObjectUtils.equals(p.context, context);
        }else{
            return false;
        }
    }
    
    @Override
    public String toString(){
        if (context != null){
            return subject + " " + predicate + " " + object + " " + context + " .";
        }else{
            return subject + " " + predicate + " " + object + " .";
        }
    }
    
}
