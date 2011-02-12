package com.mysema.rdfbean.model;

import java.util.Collection;

import javax.annotation.Nullable;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.SimplePath;

/**
 * @author tiwe
 *
 * @param <T>
 */
public class QNODE<T extends NODE> extends SimplePath<T>{

    private static final long serialVersionUID = 1134119241723346776L;

    public static final QID s = new QID("s");
    
    public static final QID p = new QID("p");
    
    public static final QNODE<NODE> o = new QNODE<NODE>(NODE.class,"o");
    
    public static final QID c = new QID("c");
    
    @Nullable
    private volatile QLIT lit;
    
    @Nullable
    private volatile QID id;
    
    public QNODE(Class<T> type, String variable) {
        super(type, variable);
    }
    
    public QNODE(Class<T> type, PathMetadata<?> metadata) {
        super(type, metadata);
    }
        
    public Block is(Object predicate, Object subject){
        return Blocks.pattern(subject, predicate, this);
    }
    
    public QID id(){
        if (id == null){
            id = new QID(getMetadata());
        }
        return id;
    }
    
    public QLIT lit(){
        if (lit == null){
            lit = new QLIT(getMetadata());
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

}
