package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collections;

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;

/**
 * @author tiwe
 *
 */
public final class Blocks {

    public static Block pattern(Object subject, Object predicate, Object object, Object context) {
        return new PatternBlock(
                convert(ID.class, subject),
                convert(UID.class, predicate),
                convert(NODE.class, object),
                convert(UID.class, context));
    }
    
    public static Block pattern(Object subject, Object predicate, Object object) {
        return new PatternBlock(
                convert(ID.class, subject),
                convert(UID.class, predicate),
                convert(NODE.class, object));
    }
    
    public static Block group(Block... blocks){
        return new GroupBlock(Arrays.asList(blocks), false);
    }
    
    public static Block filter(Block block, Predicate... filters){
        return new GroupBlock(Collections.singletonList(block), false, filters);
    }
    
    public static Block optional(Block... blocks){
        return new GroupBlock(Arrays.asList(blocks), true);
    }
    
    public static Block union(Block... blocks){
        return new UnionBlock(Arrays.asList(blocks));
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends NODE> Expression<T> convert(Class<T> cl, Object o){
        if (cl.isAssignableFrom(o.getClass())){
            return new ConstantImpl<T>((T)o);    
        }else if (o instanceof Path){
            return (Path<T>)o;
        }else{
            throw new IllegalArgumentException(o.toString());
        }
    }
    
    private Blocks(){}
    
}
