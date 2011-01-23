package com.mysema.rdfbean.model;

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class Pattern implements Block{
    
    private static final long serialVersionUID = -3450122105441266114L;

    public static Pattern create(ID subject, UID predicate, NODE object) {
        Pattern pattern = new Pattern();
        if (subject != null){
            pattern.subject = new ConstantImpl<ID>(subject);    
        }
        if (predicate != null){
            pattern.predicate = new ConstantImpl<UID>(predicate);    
        }
        if (object != null){
            pattern.object = new ConstantImpl<NODE>(object);    
        }        
        return pattern;
    }
    
    public static Pattern create(ID subject, UID predicate, NODE object, UID context) {
        Pattern pattern = create(subject, predicate, object);
        if (context != null){
            pattern.context = new ConstantImpl<UID>(context);    
        }        
        return pattern;
    }
    
    private Expression<ID> subject;
    
    private Expression<UID> predicate;
    
    private Expression<NODE> object;
    
    private Expression<UID> context;
    
    public Expression<ID> getSubject() {
        return subject;
    }

    public void setSubject(Expression<ID> subject) {
        this.subject = subject;
    }

    public Expression<UID> getPredicate() {
        return predicate;
    }

    public void setPredicate(Expression<UID> predicate) {
        this.predicate = predicate;
    }

    public Expression<NODE> getObject() {
        return object;
    }

    public void setObject(Expression<NODE> object) {
        this.object = object;
    }

    public Expression<UID> getContext() {
        return context;
    }

    public void setContext(Expression<UID> context) {
        this.context = context;
    }

    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }
    
    
    
}
