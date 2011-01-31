package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class GroupBlock implements Block{
    
    private static final long serialVersionUID = 114999121944301068L;

    private final List<Block> blocks;
    
    @Nullable
    private final Predicate filters;
    
    public GroupBlock(List<Block> blocks, Predicate... filters) {
        this.blocks = blocks;
        this.filters = ExpressionUtils.allOf(filters);
        
    }
    
    public GroupBlock(List<Block> blocks, Expression<UID> context, Predicate... filters) {
        this.blocks = blocks;
        this.filters = ExpressionUtils.allOf(filters);
    }
    
    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R, C> R accept(Visitor<R, C> v, C context) {
        if (v instanceof RDFVisitor){
            return (R)((RDFVisitor)v).visit(this, context);    
        }else{
            throw new IllegalArgumentException(v.toString());
        }       
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    @Nullable
    public Predicate getFilters() {
        return filters;
    }

}
