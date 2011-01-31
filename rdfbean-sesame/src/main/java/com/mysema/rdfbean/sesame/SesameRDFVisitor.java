package com.mysema.rdfbean.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;

import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.types.Constant;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.TemplateExpression;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.GraphBlock;
import com.mysema.rdfbean.model.GroupBlock;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.OptionalBlock;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFVisitor;
import com.mysema.rdfbean.model.UnionBlock;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.sesame.query.FunctionTransformer;

/**
 * @author tiwe
 *
 */
public class SesameRDFVisitor implements RDFVisitor<Object, Void>{
    
    private static final Map<Operator<?>,CompareOp> COMPARE_OPS = new HashMap<Operator<?>,CompareOp>();
    
    private static final Map<Operator<?>, MathOp> MATH_OPS = new HashMap<Operator<?>,MathOp>();
    
    private static final Map<Operator<?>, String> FUNCTION_OPS = new HashMap<Operator<?>, String>();
    
    static{
        new FunctionTransformer();
        
        COMPARE_OPS.put(Ops.EQ_OBJECT, CompareOp.EQ); 
        COMPARE_OPS.put(Ops.EQ_PRIMITIVE, CompareOp.EQ);
        COMPARE_OPS.put(Ops.NE_OBJECT,  CompareOp.NE);
        COMPARE_OPS.put(Ops.NE_PRIMITIVE, CompareOp.NE);
        COMPARE_OPS.put(Ops.LT, CompareOp.LT);
        COMPARE_OPS.put(Ops.BEFORE, CompareOp.LT);
        COMPARE_OPS.put(Ops.LOE, CompareOp.LE);
        COMPARE_OPS.put(Ops.BOE, CompareOp.LE);
        COMPARE_OPS.put(Ops.GT, CompareOp.GT);
        COMPARE_OPS.put(Ops.AFTER, CompareOp.GT);
        COMPARE_OPS.put(Ops.GOE, CompareOp.GE);
        COMPARE_OPS.put(Ops.AOE, CompareOp.GE);
        
        MATH_OPS.put(Ops.ADD, MathOp.PLUS);
        MATH_OPS.put(Ops.SUB, MathOp.MINUS);
        MATH_OPS.put(Ops.MULT, MathOp.MULTIPLY);
        MATH_OPS.put(Ops.DIV, MathOp.DIVIDE);      
        
        FUNCTION_OPS.put(Ops.TRIM,            "functions:trim");
        FUNCTION_OPS.put(Ops.UPPER,           "functions:upper");
        FUNCTION_OPS.put(Ops.LOWER,           "functions:lower");
        FUNCTION_OPS.put(Ops.CONCAT,          "functions:concat");
        FUNCTION_OPS.put(Ops.SUBSTR_1ARG,     "functions:substring");
        FUNCTION_OPS.put(Ops.SUBSTR_2ARGS,    "functions:substring2");
        FUNCTION_OPS.put(Ops.StringOps.SPACE, "functions:space");
        FUNCTION_OPS.put(Ops.CHAR_AT,         "functions:charAt");
        FUNCTION_OPS.put(Ops.STARTS_WITH,     "functions:startsWith");
        FUNCTION_OPS.put(Ops.ENDS_WITH,       "functions:endsWith");
        FUNCTION_OPS.put(Ops.STARTS_WITH_IC,  "functions:startsWithIc");
        FUNCTION_OPS.put(Ops.ENDS_WITH_IC,    "functions:endsWithIc");
        FUNCTION_OPS.put(Ops.STRING_CONTAINS, "functions:stringContains");
        FUNCTION_OPS.put(Ops.STRING_CONTAINS_IC, "functions:stringContainsIc");
        FUNCTION_OPS.put(Ops.EQ_IGNORE_CASE,  "functions:equalsIgnoreCase");
        FUNCTION_OPS.put(Ops.STRING_LENGTH,   "functions:stringLength");
        FUNCTION_OPS.put(Ops.INDEX_OF,        "functions:indexOf");
        FUNCTION_OPS.put(Ops.INDEX_OF_2ARGS,  "functions:indexOf2");
        FUNCTION_OPS.put(Ops.MathOps.CEIL,    "functions:ceil");
        FUNCTION_OPS.put(Ops.MathOps.FLOOR,   "functions:floor");
        FUNCTION_OPS.put(Ops.MathOps.SQRT,    "functions:sqrt");
        FUNCTION_OPS.put(Ops.MathOps.ABS,     "functions:sqrt");
        // TODO : xsd transformations
        FUNCTION_OPS.put(Ops.DateTimeOps.YEAR,"functions:year");
        FUNCTION_OPS.put(Ops.DateTimeOps.YEAR_MONTH,"functions:yearMonth");
        FUNCTION_OPS.put(Ops.DateTimeOps.MONTH,"functions:month");
        FUNCTION_OPS.put(Ops.DateTimeOps.WEEK,"functions:week");
        FUNCTION_OPS.put(Ops.DateTimeOps.DAY_OF_WEEK,"functions:dayOfWeek");
        FUNCTION_OPS.put(Ops.DateTimeOps.DAY_OF_MONTH,"functions:dayOfMonth");
        FUNCTION_OPS.put(Ops.DateTimeOps.DAY_OF_YEAR,"functions:dayOfYear");
        FUNCTION_OPS.put(Ops.DateTimeOps.HOUR,"functions:hour");
        FUNCTION_OPS.put(Ops.DateTimeOps.MINUTE,"functions:minute");
        FUNCTION_OPS.put(Ops.DateTimeOps.SECOND,"functions:second");
        FUNCTION_OPS.put(Ops.DateTimeOps.MILLISECOND,"functions:millisecond");
        
        FUNCTION_OPS.put(Ops.LIKE,            "functions:like");
        FUNCTION_OPS.put(Ops.MOD,             "functions:modulo");
    }

    private final SesameDialect dialect;
    
    private final Map<Path<?>,Var> pathToVar = new HashMap<Path<?>,Var>();
    
    private final Map<Object, Var> constantToVar = new HashMap<Object, Var>();
    
    private final VarNameIterator varNames = new VarNameIterator("_var_");
    
    private final VarNameIterator extNames = new VarNameIterator("_ext_");
    
    private final Stack<Var> graphs = new Stack<Var>();
    
    public SesameRDFVisitor(SesameDialect dialect) {
        this.dialect = dialect;
    }
    
    private Var toVar(Expression<?> expr) {
        return (Var)expr.accept(this, null);
    }
    
    private TupleExpr toTuple(Expression<?> expr) {
        return (TupleExpr)expr.accept(this, null);
    }
    
    private ValueExpr toValue(Expression<?> expr) {
        return (ValueExpr)expr.accept(this, null);
    }
    
    @Override
    public TupleExpr visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        
        // where
        TupleExpr tuple = toTuple(md.getWhere());
        
        // order
        if (!md.getOrderBy().isEmpty()){
            List<OrderElem> orderElements = new ArrayList<OrderElem>();
            for (OrderSpecifier<?> os : md.getOrderBy()){
                orderElements.add(new OrderElem(toValue(os.getTarget()), os.isAscending()));
            }
            tuple = new Order(tuple, orderElements);
        }
        
        // TODO : group by
        // TODO : having
        
        // projection
        ProjectionElemList projection = new ProjectionElemList();
        List<ExtensionElem> extensions = new ArrayList<ExtensionElem>();
        for (Expression<?> expr : md.getProjection()){
            ValueExpr val = toValue(expr);
            if (val instanceof Var){
                projection.addElement(new ProjectionElem(((Var)val).getName()));
            }else{
                String extLabel = extNames.next();
                projection.addElement(new ProjectionElem(extLabel));
                extensions.add(new ExtensionElem(val, extLabel));
            }
        }        
        if (!extensions.isEmpty()){
            tuple = new Extension(tuple, extensions);
        }
        if (!projection.getElements().isEmpty()){
            tuple = new Projection(tuple, projection);
        }
        
        // limit / offset
        QueryModifiers modifiers = md.getModifiers();
        if (modifiers.isRestricting()){
            Long limit = modifiers.getLimit();
            Long offset = modifiers.getOffset();
            tuple = new Slice(
                    tuple, 
                    offset != null ? offset.intValue() : 0, 
                    limit != null ? limit.intValue() : -1);
        }
        
        // distinct
        if (md.isDistinct()){
            tuple = new Distinct(tuple);
        }
        
        return tuple;
    }

    @Override
    public Union visit(UnionBlock expr, Void context) {
        List<TupleExpr> tuples = new ArrayList<TupleExpr>(expr.getBlocks().size());
        for (Block block : expr.getBlocks()){
            tuples.add(toTuple(block));
        } 
        return new Union(tuples);
    }

    @Override
    public TupleExpr visit(GroupBlock expr, Void context) {
        TupleExpr rv = merge(expr.getBlocks());        
        if (expr.getFilters() != null){
            rv = filter(rv, expr.getFilters());
        }
        return rv;
    }

    @Override
    public Object visit(GraphBlock expr, Void context) {
        graphs.push(toVar(expr.getContext()));
        try{
            TupleExpr rv = merge(expr.getBlocks());        
            if (expr.getFilters() != null){
                rv = filter(rv, expr.getFilters());
            }
            return rv;
        }finally{
            graphs.pop();
        }               
    }

    @Override
    public Object visit(OptionalBlock expr, Void context) {
        TupleExpr rv = merge(expr.getBlocks());        
        if (expr.getFilters() != null){
            rv = filter(rv, expr.getFilters());
        }
        return rv;
    }
    
    private TupleExpr merge(List<Block> blocks){
        List<TupleExpr> tuples = new ArrayList<TupleExpr>(blocks.size());        
        for (Block block : blocks){
            if (block instanceof OptionalBlock){
                TupleExpr right = toTuple(block);
                LeftJoin lj = new LeftJoin(tuples.size() == 1 ? tuples.get(0) : new Join(tuples), right);
                tuples = new ArrayList<TupleExpr>();
                tuples.add(lj);
            }else{
                tuples.add(toTuple(block));    
            }            
        }        
        if (tuples.size() > 1){
            return new Join(tuples);            
        }else{
            return tuples.get(0);
        }        
    }

    private TupleExpr filter(TupleExpr tuple, Predicate expr){
        ValueExpr filter = toValue(expr);
        if (filter != null){
            return new Filter(tuple, filter);    
        }else{
            return tuple;
        }
    }
    
    @Override
    public Object visit(PatternBlock expr, Void context) {
        Var subject = toVar(expr.getSubject());
        Var predicate = toVar(expr.getPredicate());
        Var object = toVar(expr.getObject());
        if (expr.getContext() != null){
            return new StatementPattern(subject, predicate, object, toVar(expr.getContext()));
        }else if (!graphs.isEmpty()){
            return new StatementPattern(subject, predicate, object, graphs.peek());
        }else{
            return new StatementPattern(subject, predicate, object);
        }
    }

    @Override
    public Var visit(Constant<?> expr, Void context) {
        Var var = constantToVar.get(expr);
        if (var == null){
            var = new Var(varNames.next(), dialect.getNode((NODE)expr.getConstant()));
            var.setAnonymous(true);
            constantToVar.put(expr, var);
        }
        return var;
    }

    @Override
    public Object visit(TemplateExpression<?> expr, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(FactoryExpression<?> expr, Void context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueExpr visit(Operation<?> expr, Void context) {
        Operator<?> op = expr.getOperator();
        if (op == Ops.AND){
            return new And(toValue(expr.getArg(0)), toValue(expr.getArg(1)));
        }else if (op == Ops.OR){
            return new Or(toValue(expr.getArg(0)), toValue(expr.getArg(1)));
        }else if (op == Ops.NOT){
            return new Not(toValue(expr.getArg(0)));
        }else if (COMPARE_OPS.containsKey(op)){
            return new Compare(toValue(expr.getArg(0)), toValue(expr.getArg(1)), COMPARE_OPS.get(op));
        }else if (MATH_OPS.containsKey(op)){
            return new MathExpr(toValue(expr.getArg(0)), toValue(expr.getArg(1)), MATH_OPS.get(op));
        }else if (op == Ops.MATCHES){
            return new Regex(new Str(toValue(expr.getArg(0))), new Str(toValue(expr.getArg(0))), null);
        }else if (op == Ops.STRING_IS_EMPTY){    
            return new Regex(new Str(toValue(expr.getArg(0))), "", false);
        }else if (op == Ops.IS_NULL){    
            return new Not(new Bound(toVar(expr.getArg(0))));
        }else if (op == Ops.IS_NOT_NULL){    
            return new Bound(toVar(expr.getArg(0)));            
        }else if (op == Ops.EXISTS){
            return new Exists(toTuple(expr.getArg(0)));
        }else if (op == Ops.DELEGATE){
            return toValue(expr.getArg(0));
        }else if (FUNCTION_OPS.containsKey(op)){
            List<ValueExpr> args = new ArrayList<ValueExpr>(expr.getArgs().size());
            for (Expression<?> e : expr.getArgs()){
                args.add(toValue(e));
            }
            return new FunctionCall(FUNCTION_OPS.get(op), args);
        }else{
            throw new IllegalArgumentException(expr.toString());
        }
    }

    @Override
    public Var visit(Path<?> expr, Void context) {
        Var var = pathToVar.get(expr);
        if (var == null){
            var = new Var(expr.toString());
            pathToVar.put(expr, var);
        }
        return var;
    }

    @Override
    public TupleExpr visit(SubQueryExpression<?> expr, Void context) {
        return visit(expr.getMetadata(), QueryLanguage.TUPLE);
    }

    @Override
    public Object visit(ParamExpression<?> expr, Void context) {
        throw new UnsupportedOperationException();
    }

}
