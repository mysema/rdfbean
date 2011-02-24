package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.ObjectUtils;

import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Ops;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.BooleanOperation;

public class GraphBlock implements ContainerBlock{

    private static final long serialVersionUID = -4450740702187022383L;

    private final List<Block> blocks;

    @Nullable
    private final Predicate filters;

    private final Expression<UID> context;

    public GraphBlock(Expression<UID> context, List<Block> blocks, Predicate... filters) {
        this.blocks = blocks;
        this.context = context;
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

    public Expression<UID> getContext() {
        return context;
    }

    @Override
    public BooleanExpression exists(){
        return BooleanOperation.create(Ops.EXISTS, this);
    }

    @Override
    public int hashCode(){
        return context.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if (o == this){
            return true;
        }else if (o instanceof GraphBlock){
            GraphBlock gb = (GraphBlock)o;
            return context.equals(gb.context) && ObjectUtils.equals(filters, gb.filters) && blocks.equals(gb.blocks);
        }else{
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("GRAPH ").append(context).append("{ ");
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
