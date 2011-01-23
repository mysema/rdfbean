package com.mysema.rdfbean.model;

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class PatternBlock implements Block{
    
    private static final long serialVersionUID = -3450122105441266114L;

    @SuppressWarnings("unchecked")
    public static PatternBlock create(Object subject, Object predicate, Object object) {
        PatternBlock pattern = new PatternBlock();
        if (subject != null){
            if (subject instanceof ID){
                pattern.subject = new ConstantImpl<ID>((ID)subject);    
            }else{
                pattern.subject = (Path<ID>)subject;
            }                
        }
        if (predicate != null){
            if (predicate instanceof UID){
                pattern.predicate = new ConstantImpl<UID>((UID)predicate);    
            }else{
                pattern.predicate = (Path<UID>)predicate;
            }                
        }
        if (object != null){
            if (object instanceof NODE){
                pattern.object = new ConstantImpl<NODE>((NODE)object);    
            }else{
                pattern.object = (Path<NODE>)object;
            }                
        }        
        return pattern;
    }
    
    private Expression<ID> subject;
    
    private Expression<UID> predicate;
    
    private Expression<NODE> object;
    
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

    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return (R)((SPARQLVisitor)v).visit(this, null);
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }
        
}
