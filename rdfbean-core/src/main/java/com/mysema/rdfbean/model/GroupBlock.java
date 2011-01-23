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
public class GroupBlock implements Block{
    
    private static final long serialVersionUID = 114999121944301068L;

    public static GroupBlock create(Block... blocks){
        GroupBlock group = new GroupBlock();
        group.blocks.addAll(Arrays.asList(blocks));
        return group;
    }
    
    public static GroupBlock filter(Block block, Predicate... filters){
        GroupBlock group = new GroupBlock();
        group.blocks.add(block);
        group.filters.addAll(Arrays.asList(filters));
        return group;
    }
    
    public static GroupBlock optional(Block... blocks){
        GroupBlock group = create(blocks);
        group.optional = true;
        return group;
    }

    private final List<Block> blocks = new ArrayList<Block>();
    
    private final List<Predicate> filters = new ArrayList<Predicate>();
    
    private boolean optional;
    
    private Expression<UID> context;
    
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

}
