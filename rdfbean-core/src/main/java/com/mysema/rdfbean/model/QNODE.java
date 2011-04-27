package com.mysema.rdfbean.model;

import javax.annotation.Nullable;

import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.Param;

/**
 * @author tiwe
 *
 * @param <T>
 */
public class QNODE<T extends NODE> extends Param<T>{

    private static final long serialVersionUID = 1134119241723346776L;

    public static final QID s = new QID("s");

    public static final QUID p = new QUID("p");

    public static final QNODE<NODE> o = new QNODE<NODE>(NODE.class,"o");

    public static final QID c = new QID("c");

    public static final QID type = new QID("type");

    public static final QID typeContext = new QID("typeContext");

    public static final QNODE<NODE> first = new QNODE<NODE>(NODE.class, "first");

    public static final QID rest = new QID("rest");

    @Nullable
    private volatile QLIT lit;

    @Nullable
    private volatile QID id;

    @Nullable
    @SuppressWarnings("unchecked")
    private volatile OrderSpecifier asc, desc;

    public QNODE(Class<T> type, String variable) {
        super(type, variable);
    }

    public PatternBlock is(Object predicate, Object subject){
        return Blocks.pattern(subject, predicate, this);
    }

    @SuppressWarnings("unchecked")
    public OrderSpecifier asc(){
        if (asc == null){
            asc = new OrderSpecifier(Order.ASC, this);
        }
        return asc;
    }

    @SuppressWarnings("unchecked")
    public OrderSpecifier desc(){
        if (desc == null){
            desc = new OrderSpecifier(Order.DESC, this);
        }
        return desc;
    }

    public QID id(){
        if (id == null){
            id = new QID(getName());
        }
        return id;
    }

    public QLIT lit(){
        if (lit == null){
            lit = new QLIT(getName());
        }
        return lit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this){
            return true;
        }else if (o instanceof QNODE<?>){
            QNODE<?> other = (QNODE<?>)o;
            return other.getName().equals(getName());
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return getName().hashCode();
    }

}
