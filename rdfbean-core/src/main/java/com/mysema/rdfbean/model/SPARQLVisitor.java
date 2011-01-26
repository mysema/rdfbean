package com.mysema.rdfbean.model;

import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.support.SerializerBase;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.SubQueryExpression;

/**
 * @author tiwe
 *
 */
public class SPARQLVisitor extends SerializerBase<SPARQLVisitor>{
    
    @Nullable
    private PatternBlock lastPattern;

    public SPARQLVisitor(SPARQLTemplates templates) {
        super(templates);
    }
    
    public SPARQLVisitor() {
        super(SPARQLTemplates.DEFAULT);
    }
    
    public void visit(QueryMetadata md, QueryLanguage<?,?> queryType) {
        QueryModifiers mod = md.getModifiers();
        // select
        if (queryType == QueryLanguage.TUPLE){
            append("SELECT ");
            if (md.isDistinct()){
                append("DISTINCT ");
            }
            handle(" ", md.getProjection());
            append("\n");
            
        // ask
        }else if (queryType == QueryLanguage.BOOLEAN){
            append("ASK \n");
            
        // construct
        }else if (queryType == QueryLanguage.GRAPH){
            if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof GroupBlock){
                append("CONSTRUCT ").handle("", md.getProjection()).append("\n");
            }else{
                append("CONSTRUCT { ").handle("", md.getProjection()).append("}\n");    
            }            
        }
        lastPattern = null;
        
        // where
        if (md.getWhere() != null){
            append("WHERE \n  ");
            if (md.getWhere() instanceof GroupBlock){
                handle(md.getWhere());                
            }else{
                append("{ ");
                handle(md.getWhere());
                append("}");    
            }
            append("\n");                
        }      
        
        // order
        if (!md.getOrderBy().isEmpty()){
            append("ORDER BY ");
            boolean first = true;            
            for (OrderSpecifier<?> order : md.getOrderBy()){
                if (!first){
                    append(" ");                    
                }
                if (order.isAscending()){
                    handle(order.getTarget());
                }else{
                    append("DESC(").handle(order.getTarget()).append(")");
                }
                first = false;
            }
            append("\n");
        }
        
        // group by
        if (!md.getGroupBy().isEmpty()){
            append("GROUP BY ").handle(" ", md.getGroupBy()).append("\n");
        }
        
        // having
        if (md.getHaving() != null){
            append("HAVING (").handle(md.getHaving()).append(")\n");            
        }    
        
        // limit        
        if (mod.getLimit() != null){
            append("LIMIT ").append(mod.getLimit().toString()).append("\n");
        }
        
        // offset
        if (mod.getOffset() != null){
            append("OFFSET ").append(mod.getOffset().toString()).append("\n");
        }
    }
    
    @Override
    public Void visit(SubQueryExpression<?> expr, Void context) {
        visit(expr.getMetadata(), QueryLanguage.TUPLE);
        return null;
    }

    @Nullable
    public Void visit(UnionBlock expr, @Nullable Void context) {
        lastPattern = null;
        boolean first = true;
        for (Block block : expr.getBlocks()){
            if (!first){
                append("UNION ");
            }
            if (block instanceof PatternBlock){
                append("{ ").handle(block).append("} ");
            }else{
                handle(block);
            }
            lastPattern = null;
            first = false;
        }
        return null;
    }
    
    @Nullable
    public Void visit(GroupBlock expr, @Nullable Void context) {
        lastPattern = null;
        if (expr.getContext() != null){
            
        }else if (expr.isOptional()){
            append("OPTIONAL ");
        }
        append("{ ");
        for (Block block : expr.getBlocks()){
            if (lastPattern != null && !(block instanceof PatternBlock)){
                append(".\n  ");
                lastPattern = null;
            }
            handle(block);
        }
        if (expr.getFilters() != null){
            if (lastPattern != null){
                append(". ");
            }
            handle(expr.getFilters());
        }
        append("} ");
        lastPattern = null;
        return null;
    }
    
    @Override
    public Void visit(Constant<?> expr, Void context) {
        if (!getConstantToLabel().containsKey(expr.getConstant())) {
            String constLabel = "_c" + (getConstantToLabel().size() + 1);
            getConstantToLabel().put(expr.getConstant(), constLabel);
            append("?" + constLabel);
        } else {
            append("?" + getConstantToLabel().get(expr.getConstant()));
        }
        return null;
    }
    
    @Nullable
    public Void visit(PatternBlock expr, @Nullable Void context) {
        if (lastPattern == null || !lastPattern.getSubject().equals(expr.getSubject())){
            if (lastPattern != null){
                append(".\n  ");
            }
            handle(expr.getSubject()).append(" ");
            handle(expr.getPredicate()).append(" ");
            
        }else if (!lastPattern.getPredicate().equals(expr.getPredicate())){
            append("; ");
            handle(expr.getPredicate()).append(" ");
            
        }else{
            append(", ");
        }
        
        handle(expr.getObject()).append(" ");
        lastPattern = expr;
        return null;
    }
    
    @Override
    protected void visitOperation(Class<?> type, Operator<?> operator, List<Expression<?>> args) {
        if (operator == Ops.AND && (args.get(0) instanceof Block || args.get(1) instanceof Block)){
            handle("", args);
        }else{
            append("FILTER(");
            super.visitOperation(type, operator, args);
            append(") ");            
        }
    }
    
}
