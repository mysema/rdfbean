package com.mysema.rdfbean.model;

import java.util.List;

import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;

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
        }else if (v instanceof ToStringVisitor){
            return (R)toString();
        } else if (v.getClass().getName().equals("com.mysema.query.types.ExtractorVisitor")) {    
            return (R)this;
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
    public BooleanExpression exists(){
        return BooleanOperation.create(Ops.EXISTS, this);
    }

    @Override
    public int hashCode(){
        return blocks.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if (o instanceof UnionBlock){
            UnionBlock gb = (UnionBlock)o;
            return blocks.equals(gb.blocks);
        }else{
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for (Block block : blocks){
            if (builder.length() > 0){
                builder.append(" UNION ");
            }
            builder.append(block.toString());
        }
        return builder.toString();
    }

}
