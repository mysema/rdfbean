package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mysema.query.types.Predicate;
import com.mysema.query.types.Visitor;

public class Union implements Block{
    
    private static final long serialVersionUID = -5081510328796327230L;

    public static Union create(Block... blocks){
        Union union = new Union();
        union.blocks.addAll(Arrays.asList(blocks));
        return union;
    }
    
    private final List<Block> blocks = new ArrayList<Block>();
        
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
