package com.mysema.rdfbean.model;

import java.util.Collection;

import javax.annotation.Nullable;

import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.Param;

/**
 * @author tiwe
 *
 * @param <T>
 */
public class QNODE<T extends NODE> extends Param<T>{

    private static final long serialVersionUID = 1134119241723346776L;

    public static final QID s = new QID("s");

    public static final QID p = new QID("p");

    public static final QNODE<NODE> o = new QNODE<NODE>(NODE.class,"o");
    
    public static final QID c = new QID("c");

    public static final QID type = new QID("type");
    
    public static final QID typeContext = new QID("typeContext");
    
    @Nullable
    private volatile QLIT lit;

    @Nullable
    private volatile QID id;

    public QNODE(Class<T> type, String variable) {
        super(type, variable);
    }

    public Block is(Object predicate, Object subject){
        return Blocks.pattern(subject, predicate, this);
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
    public BooleanExpression in(T... values){
        BooleanExpression[] ors = new BooleanExpression[values.length];
        int i = 0;
        for (T value : values){
            ors[i++] = eq(value);
        }
        return BooleanExpression.anyOf(ors);
    }

    @Override
    public BooleanExpression in(Collection<? extends T> values){
        BooleanExpression[] ors = new BooleanExpression[values.size()];
        int i = 0;
        for (T value : values){
            ors[i++] = eq(value);
        }
        return BooleanExpression.anyOf(ors);
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
