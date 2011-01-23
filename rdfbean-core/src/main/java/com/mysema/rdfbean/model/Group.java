package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class Group implements Block{
    
    private static final long serialVersionUID = 114999121944301068L;

    public static Group create(Block... blocks){
        Group group = new Group();
        group.blocks.addAll(Arrays.asList(blocks));
        return group;
    }
    
    public static Group optional(Block... blocks){
        Group group = create(blocks);
        group.optional = true;
        return group;
    }
    
    public static Group union(Block... blocks){
        Group group = create(blocks);
        group.union = true;
        return group;
    }

    private final List<Block> blocks = new ArrayList<Block>();
    
    private final List<Predicate> filters = new ArrayList<Predicate>();
    
    private boolean optional;
    
    private Expression<UID> context;

    private boolean union;
    
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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Predicate> getFilters() {
        return filters;
    }

    public boolean isOptional() {
        return optional;
    }

    public Expression<UID> getContext() {
        return context;
    }

    public boolean isUnion() {
        return union;
    }
    
    
    
}
