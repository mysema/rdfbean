package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.mysema.commons.lang.Assert;
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
        this.blocks = Assert.notEmpty(blocks,"blocks");
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
            return Objects.equal(filters, gb.filters) && blocks.equals(gb.blocks);
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
