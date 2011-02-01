package com.mysema.rdfbean.model;

import java.util.List;

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
    
    @Override
    public Predicate exists(){
        return new PredicateOperation(Ops.EXISTS, new ConstantImpl<Block>(this));
    }

}
