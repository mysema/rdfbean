package com.mysema.rdfbean.rdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.QueryMetadata;
import com.mysema.query.sql.SQLCommonQuery;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.types.*;
import com.mysema.query.types.Operation;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.query.VarNameIterator;

/**
 * @author tiwe
 *
 */
public class RDBRDFVisitor implements RDFVisitor<Object, QueryMetadata>{

    private final RDBContext context;
    
    private Map<Expression<?>, Expression<?>> exprToSymbol = new HashMap<Expression<?>, Expression<?>>();
    
    private Map<Expression<?>, Path<Long>> exprToMapped = new HashMap<Expression<?>, Path<Long>>();
    
    private final Stack<Expression<UID>> graphs = new Stack<Expression<UID>>();
    
    private final Transformer<Long, NODE> transformer;
    
    private final VarNameIterator stmts = new VarNameIterator("stmts");
    
    private final VarNameIterator symbols = new VarNameIterator("symbols");
    
    private SQLCommonQuery<?> query;
    
    private boolean inOptional;   
    
    private boolean firstSource = true;
    
    private boolean asLiteral = false;
    
    public RDBRDFVisitor(RDBContext context, Transformer<Long, NODE> transformer) {
        this.context = context;
        this.transformer = transformer;
    }
    
    private Long getId(NODE node) {
        return context.getNodeId(node);
    }
    
    private Long getId(Constant<NODE> constant){
        return context.getNodeId(constant.getConstant());
    }
    
    private Expression<?> handle(Expression<?> expr, QueryMetadata md){
        return (Expression<?>) expr.accept(this, md);
    }
    
    private boolean needsSymbolResolving(Operation<?> op) {
        if (Ops.equalsOps.contains(op.getOperator())
         || Ops.notEqualsOps.contains(op.getOperator())
         || op.getOperator() == Ops.IN
         || op.getOperator() == Ops.ORDINAL){
            for (Expression<?> arg : op.getArgs()){
                if (!(arg instanceof Path<?>) 
                  && !(arg instanceof ParamExpression<?>)
                  && !(arg instanceof Constant<?>)){
                    return true;
                }
            }
            return false;
        }else{
            return true;
        }
    }
        
    @Override
    public Object visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        query = context.createQuery();
        // FIXME
        boolean asCount = md.getProjection().size() == 1 && md.getProjection().get(0).toString().equals("count(*)");
        
        // where
        handle(md.getWhere(), md);
        
        // group by
        for (Expression<?> gb : md.getGroupBy()){
            query.groupBy(handle(gb, md));
        }
        
        // having
        if (md.getHaving() != null){
            query.having((Predicate)handle(md.getHaving(), md));
        }
        
        if (!asCount){
            // order by
            asLiteral = true;
            for (OrderSpecifier<?> order : md.getOrderBy()){
                query.orderBy(new OrderSpecifier(order.getOrder(), handle(order.getTarget(), md)));
            }
            asLiteral = false;
            
            // limit + offset
            query.restrict(md.getModifiers());
            
            // distinct
            if (md.isDistinct()){
                query.distinct();
            }            
        }
        
        // select
        if (queryType.equals(QueryLanguage.TUPLE)){
            List<String> variables = new ArrayList<String>();
            List<Expression<?>> pr = new ArrayList<Expression<?>>();
            for (Expression<?> expr : md.getProjection()){
                variables.add(expr.toString());
                pr.add(handle(expr, md));
            }
            return new TupleQueryImpl((SQLQuery)query, variables, pr, transformer);
                
        }else{
            // TODO
            return null;    
        }
        
        
    }
    
    @Override
    public Constant<?> visit(Constant<?> constant, QueryMetadata context) {
        if (NODE.class.isAssignableFrom(constant.getConstant().getClass())){
            NODE node = (NODE)constant.getConstant();
            if (asLiteral){                
                return ConstantImpl.create(node.getValue());                    
            }else{
                return ConstantImpl.create(getId(node));
            }            
        }else{
            throw new IllegalArgumentException(constant.toString());
        }
    }

    @Override
    public Object visit(FactoryExpression<?> expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(GraphBlock expr, QueryMetadata context) {
        graphs.push(expr.getContext());
        visit((ContainerBlock)expr, context);
        graphs.pop();
        return null;
    }

    @Override
    public Object visit(GroupBlock expr, QueryMetadata context) {
        return visit((ContainerBlock)expr, context);
    }
    


    @SuppressWarnings("unchecked")
    @Override
    public Object visit(Operation<?> expr, QueryMetadata context) {
        List<Expression<?>> args = new ArrayList<Expression<?>>(expr.getArgs().size());
        boolean asLit = asLiteral;
        asLiteral = needsSymbolResolving(expr);
        for (Expression<?> arg : expr.getArgs()){
            args.add(handle(arg, context));
        }
        asLiteral = asLit;
        if (expr.getType().equals(Boolean.class)){
            return new PredicateOperation((Operator)expr.getOperator(), args);
        }else{
            return new OperationImpl(expr.getType(), expr.getOperator(), args);
        }
    }
    
    @Override
    public Object visit(OptionalBlock expr, QueryMetadata context) {
        boolean inOpt = inOptional;
        inOptional = true;
        visit((ContainerBlock)expr, context);
        inOptional = inOpt;
        return null;
    }
    
    private Object visit(ContainerBlock expr, QueryMetadata context){
        for (Block block : expr.getBlocks()){
            handle(block, context);
        }
        if (expr.getFilters() != null){
            query.where((Predicate)handle(expr.getFilters(), context));
        }
        return null;
    }

    @Override
    public Expression<?> visit(ParamExpression<?> expr, QueryMetadata context) {
        return visitPathOrParam(expr, context);
    }

    @Override
    public Expression<?> visit(Path<?> expr, QueryMetadata context) {
        return visitPathOrParam(expr, context);
    }

    private Expression<?> visitPathOrParam(Expression<?> expr, QueryMetadata context){
        if (asLiteral){
            if (exprToSymbol.containsKey(expr)){
                return exprToSymbol.get(expr);
            }else{
                QSymbol symbol = new QSymbol(symbols.next());
                query.leftJoin(symbol).on(symbol.id.eq(exprToMapped.get(expr)));
                exprToSymbol.put(expr, symbol.lexical);
                return symbol.lexical;
            }            
        }else{
            return exprToMapped.get(expr);    
        }  
    }
    
    @Override
    public Object visit(PatternBlock expr, QueryMetadata context) {
        QStatement stmt = new QStatement(stmts.next());
        BooleanBuilder filters = new BooleanBuilder();
        filters.and(visitPatternElement(context, stmt.subject, expr.getSubject()));
        filters.and(visitPatternElement(context, stmt.predicate, expr.getPredicate()));
        filters.and(visitPatternElement(context, stmt.object, expr.getObject()));
        Expression<UID> c = expr.getContext();
        if (c == null && !graphs.isEmpty()){
            c = graphs.peek();
        }
        if (c != null){
            filters.and(visitPatternElement(context, stmt.model, c));    
        }
        
        if (firstSource){
            query.from(stmt).where(filters.getValue());
        }else if (inOptional){    
            query.leftJoin(stmt).on(filters.getValue());
        }else{
            query.innerJoin(stmt).on(filters.getValue());
        }        
        firstSource = false;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Predicate visitPatternElement(QueryMetadata context, NumberPath<Long> path, Expression<? extends NODE> s) {
        if (s instanceof Constant<?>){
            return path.eq(getId((Constant)s));
        }else {
            if (exprToMapped.containsKey(s)){
                return path.eq(exprToMapped.get(s));
            }else{
                exprToMapped.put(s, path);
                if (s instanceof ParamExpression<?>){
                    Object constant = context.getParams().get(s);
                    if (constant != null){
                        return path.eq(getId((NODE)constant));    
                    }                    
                }
            }
        }
        return null;
    }

    @Override
    public SubQueryExpression<?> visit(SubQueryExpression<?> expr, QueryMetadata context) {
        SQLCommonQuery<?> q = query;
        boolean firstS = firstSource;
        
        firstSource = true;
        query = new SQLSubQuery();
        Map<Expression<?>, Path<Long>> exprToM = exprToMapped;
        Map<Expression<?>, Expression<?>> exprToS = exprToSymbol;
        exprToMapped = new HashMap<Expression<?>, Path<Long>>(exprToMapped);
        exprToSymbol = new HashMap<Expression<?>, Expression<?>>(exprToSymbol);
        
        QueryMetadata md = expr.getMetadata();
        
        // where
        handle(md.getWhere(), md);
        
        // group by
        for (Expression<?> gb : md.getGroupBy()){
            query.groupBy(handle(gb, md));
        }
        
        // having
        if (md.getHaving() != null){
            query.having((Predicate)handle(md.getHaving(), md));
        }
        
        // order by
        boolean asLit = asLiteral;
        asLiteral = true;
        for (OrderSpecifier<?> order : md.getOrderBy()){
            query.orderBy(new OrderSpecifier(order.getOrder(), handle(order.getTarget(), md)));
        }
        asLiteral = asLit;
        
        // limit + offset
        query.restrict(md.getModifiers());
        
        // distinct
        if (md.isDistinct()){
            query.distinct();
        }
        
        List<Expression<?>> projection = new ArrayList<Expression<?>>();
        for (Expression<?> e : md.getProjection()){
            projection.add(handle(e, md));
        }        
        SubQueryExpression<?> sqe = ((SQLSubQuery)query).list(projection.toArray(new Expression[projection.size()])); 
        
        firstSource = firstS;
        query = q;
        exprToMapped = exprToM;
        exprToSymbol = exprToS;
        
        return sqe;
    }

    @Override
    public Object visit(TemplateExpression<?> expr, QueryMetadata context) {
        List<Expression<?>> args = new ArrayList<Expression<?>>(expr.getArgs());
        for (Expression<?> arg : expr.getArgs()){
            args.add(handle(arg, context));
        }
        if (expr.getType().equals(Boolean.class)){
            return new BooleanTemplate(expr.getTemplate(), args);
        }else{
            return new TemplateExpressionImpl<Object>(expr.getType(), expr.getTemplate(), args);
        }
    }

    @Override
    public Object visit(UnionBlock expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

}
