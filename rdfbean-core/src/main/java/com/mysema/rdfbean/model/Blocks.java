package com.mysema.rdfbean.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;

/**
 * @author tiwe
 *
 */
@SuppressWarnings("unchecked")
public final class Blocks {

    private static final Map<UID, Constant<UID>> CACHE = new HashMap<UID, Constant<UID>>(1024);

    static{
        for (UID uid : Nodes.all){
            CACHE.put(uid, new ConstantImpl<UID>(UID.class, uid));
        }
    }

    public static final Block S_TYPE = pattern(QNODE.s, RDF.type, QNODE.type, QNODE.typeContext);

    public static final Block S_FIRST = pattern(QNODE.s, RDF.first, QNODE.first);

    public static final Block S_REST = pattern(QNODE.s, RDF.rest, QNODE.rest);

    public static final Block SPO = pattern(QNODE.s, QNODE.p, QNODE.o);

    public static final Block SPOC = pattern(QNODE.s, QNODE.p, QNODE.o, QNODE.c);

    public static PatternBlock pattern(Object subject, Object predicate, Object object, @Nullable Object context) {
        return new PatternBlock(
                convert(ID.class, subject),
                convert(UID.class, predicate),
                convert(NODE.class, object),
                context != null ? convert(UID.class, context) : null);
    }

    public static PatternBlock pattern(Object subject, Object predicate, Object object) {
        return new PatternBlock(
                convert(ID.class, subject),
                convert(UID.class, predicate),
                convert(NODE.class, object));
    }

    public static GroupBlock group(Block... blocks){
        return new GroupBlock(Arrays.asList(blocks));
    }

    public static GroupBlock filter(Block block, Predicate... filters){
        return new GroupBlock(Collections.singletonList(block), filters);
    }

    public static OptionalBlock optional(Block... blocks){
        return new OptionalBlock(Arrays.asList(blocks));
    }
    
    public static OptionalBlock optional(List<Block> blocks, Predicate... predicates){
        return new OptionalBlock(blocks, predicates);
    }    
    
    public static OptionalBlock optionalFilter(Block block, Predicate... predicates){
        return new OptionalBlock(Collections.singletonList(block), predicates);
    }
    
    public static UnionBlock union(Block... blocks){
        return new UnionBlock(Arrays.asList(blocks));
    }

    public static GraphBlock graph(Expression<UID> context, Block... blocks){
        return new GraphBlock(context, Arrays.asList(blocks));
    }

    public static GraphBlock graphFilter(Expression<UID> context, Block block, Predicate... filters){
        return new GraphBlock(context, Collections.singletonList(block), filters);
    }

    private static <T extends NODE> Expression<T> convert(Class<T> cl, Object o){
        if (o instanceof UID && CACHE.containsKey(o)){
            return (Expression<T>)CACHE.get(o);
        }else if (cl.isAssignableFrom(o.getClass())){
            return new ConstantImpl<T>((T)o);
        }else if (o instanceof Expression && NODE.class.isAssignableFrom(((Expression)o).getType())){
            return (Expression<T>)o;
        }else{
            throw new IllegalArgumentException(o.toString());
        }
    }

    private Blocks(){}

}
