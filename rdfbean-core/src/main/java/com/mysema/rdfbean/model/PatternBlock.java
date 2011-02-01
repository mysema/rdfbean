package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
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
        
}
