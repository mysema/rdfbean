package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.ObjectUtils;

import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;

public class OptionalBlock implements ContainerBlock{

    private static final long serialVersionUID = 7345721586959129539L;

    private final List<Block> blocks;

    @Nullable
    private final Predicate filters;

    public OptionalBlock(List<Block> blocks,  Predicate... filters) {
        this.blocks = blocks;
        this.filters = ExpressionUtils.allOf(filters);
    }

    @Override
    public Predicate not() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        if (v instanceof RDFVisitor){
            return (R)((RDFVisitor)v).visit(this, context);
        }else if (v instanceof ToStringVisitor){
            return (R)toString();
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
        }else if (o instanceof OptionalBlock){
            OptionalBlock gb = (OptionalBlock)o;
            return ObjectUtils.equals(filters, gb.filters) && blocks.equals(gb.blocks);
        }else{
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("OPTIONAL { ");
        for (Block block : blocks){
            builder.append(block.toString()).append(" ");
        }
        if (filters != null){
            builder.append(" FILTER(").append(filters).append(")");
        }
        builder.append(" }");
        return builder.toString();
    }

}
