package com.mysema.rdfbean.model;

import java.util.List;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

/**
 * @author tiwe
 *
 */
public class UnionBlock implements Block{
    
    private static final long serialVersionUID = -5081510328796327230L;

    private final List<Block> blocks;
        
    public UnionBlock(List<Block> blocks) {
        this.blocks = blocks;
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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

}
