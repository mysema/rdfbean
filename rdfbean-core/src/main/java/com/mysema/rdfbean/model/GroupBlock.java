package com.mysema.rdfbean.model;

import java.util.List;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class GroupBlock implements Block{
    
    private static final long serialVersionUID = 114999121944301068L;

    private final List<Block> blocks;
    
    private final Predicate filters;
    
    private final boolean optional;
    
    private final Expression<UID> context;
    
    public GroupBlock(List<Block> blocks, boolean optional, Predicate... filters) {
        this.blocks = blocks;
        this.optional = optional;
        this.context = null;
        BooleanBuilder builder = new BooleanBuilder();
        for (Predicate filter : filters){
            builder.and(filter);    
        }  
        this.filters = builder.getValue();
        
    }
    
    public GroupBlock(List<Block> blocks, Expression<UID> context, Predicate... filters) {
        this.blocks = blocks;
        this.optional = false;
        this.context = context;
        BooleanBuilder builder = new BooleanBuilder();
        for (Predicate filter : filters){
            builder.and(filter);    
        }  
        this.filters = builder.getValue();
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

    public List<Block> getBlocks() {
        return blocks;
    }

    public Predicate getFilters() {
        return filters;
    }

    public boolean isOptional() {
        return optional;
    }

    public Expression<UID> getContext() {
        return context;
    }

}
