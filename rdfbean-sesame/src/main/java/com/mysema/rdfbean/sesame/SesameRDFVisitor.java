package com.mysema.rdfbean.sesame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import org.openrdf.model.Literal;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.MathExpr.MathOp;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.types.*;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.query.VarNameIterator;

/**
 * @author tiwe
 *
 */
public class SesameRDFVisitor implements RDFVisitor<Object, QueryMetadata>{

    private static final ValueConstant CASE_INSENSITIVE = new ValueConstant(new LiteralImpl("i"));

    private static final Map<Operator<?>,CompareOp> COMPARE_OPS = new HashMap<Operator<?>,CompareOp>();

    private static final Map<Operator<?>, MathOp> MATH_OPS = new HashMap<Operator<?>,MathOp>();

    private static final Map<Operator<?>, String> FUNCTION_OPS = new HashMap<Operator<?>, String>();

    static{
        SesameFunctions.init();

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

        // string
        FUNCTION_OPS.put(Ops.TRIM,            "functions:trim");
        FUNCTION_OPS.put(Ops.UPPER,           "functions:upper");
        FUNCTION_OPS.put(Ops.LOWER,           "functions:lower");
        FUNCTION_OPS.put(Ops.CONCAT,          "functions:concat");
        FUNCTION_OPS.put(Ops.SUBSTR_1ARG,     "functions:substring");
        FUNCTION_OPS.put(Ops.SUBSTR_2ARGS,    "functions:substring2");
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
        FUNCTION_OPS.put(Ops.LIKE,            "functions:like");
        FUNCTION_OPS.put(Ops.StringOps.SPACE, "functions:space");

        // math
        FUNCTION_OPS.put(Ops.MathOps.CEIL,    "functions:ceil");
        FUNCTION_OPS.put(Ops.MathOps.FLOOR,   "functions:floor");
        FUNCTION_OPS.put(Ops.MathOps.SQRT,    "functions:sqrt");
        FUNCTION_OPS.put(Ops.MathOps.ABS,     "functions:abs");
        FUNCTION_OPS.put(Ops.MOD,             "functions:modulo");

        // date / time
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

        // other
        FUNCTION_OPS.put(Ops.COALESCE, "functions:coalesce");


    }

    private final SesameDialect dialect;

    private final Map<Path<?>,Var> pathToVar = new HashMap<Path<?>,Var>();

    private final Map<ParamExpression<?>,Var> paramToVar = new HashMap<ParamExpression<?>,Var>();

    private final Map<Object, Var> constantToVar = new HashMap<Object, Var>();

    private final VarNameIterator varNames = new VarNameIterator("__v");

    private final VarNameIterator extNames = new VarNameIterator("__e");

    private final Stack<Var> graphs = new Stack<Var>();

    public SesameRDFVisitor(SesameDialect dialect) {
        this.dialect = dialect;
    }

    private Var toVar(Expression<?> expr, QueryMetadata md) {
        return (Var)expr.accept(this, md);
    }

    private TupleExpr toTuple(Expression<?> expr, QueryMetadata md) {
        return (TupleExpr)expr.accept(this, md);
    }

    @Nullable
    private ValueExpr toValue(Expression<?> expr, QueryMetadata md) {
        return (ValueExpr)expr.accept(this, md);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TupleExpr visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        List<Constant<UID>> fromUIDs = new ArrayList<Constant<UID>>();
        for (JoinExpression join : md.getJoins()){
            fromUIDs.add((Constant<UID>)join.getTarget());
        }
        // where
        TupleExpr tuple;
        if (fromUIDs.isEmpty()) {
            tuple = toTuple(md.getWhere(), md);
        } else if (fromUIDs.size() == 1) {
            graphs.push(visit(fromUIDs.get(0), md));
            tuple = toTuple(md.getWhere(), md);
            graphs.pop();
        } else {
            QUID g = new QUID("__g");
            graphs.push(visit(g, md));
            BooleanBuilder b = new BooleanBuilder();
            for (Constant<UID> co : fromUIDs){
                b.or(g.eq(co));
            }
            tuple = filter(toTuple(md.getWhere(), md), b.getValue(), md);
            graphs.pop();
        }

        if (queryType == QueryLanguage.BOOLEAN){
            return tuple;
        }

        // order
        if (!md.getOrderBy().isEmpty()){
            List<OrderElem> orderElements = new ArrayList<OrderElem>();
            for (OrderSpecifier<?> os : md.getOrderBy()){
                orderElements.add(new OrderElem(toValue(os.getTarget(), md), os.isAscending()));
            }
            tuple = new Order(tuple, orderElements);
        }

        // TODO : group by
        // TODO : having

        // projection
        ProjectionElemList projection = new ProjectionElemList();
        List<ProjectionElemList> projectionElements = new ArrayList<ProjectionElemList>();
        List<ExtensionElem> extensions = new ArrayList<ExtensionElem>();
        if (queryType == QueryLanguage.TUPLE){
            for (Expression<?> expr : md.getProjection()){
                ValueExpr val = toValue(expr, md);
                if (val instanceof Var){
                    projection.addElement(new ProjectionElem(((Var)val).getName()));
                }else{
                    String extLabel = extNames.next();
                    projection.addElement(new ProjectionElem(extLabel));
                    extensions.add(new ExtensionElem(val, extLabel));
                }
            }
        }else{
            for (Expression<?> expr : md.getProjection()){
                Stack<Block> blocks = new Stack<Block>();
                blocks.addAll((List)md.getProjection());
                while (!blocks.isEmpty()){
                    Block bl = blocks.pop();
                    // TODO : shorten
                    if (bl instanceof PatternBlock){
                        PatternBlock pa = (PatternBlock)expr;
                        ProjectionElemList p = new ProjectionElemList();
                        ValueExpr subject = toValue(pa.getSubject(), md);
                        if (subject instanceof Var){
                            Var v = (Var)subject;
                            p.addElement(new ProjectionElem(v.getName(),"subject"));
                            if (v.getValue() != null){
                                extensions.add(new ExtensionElem(subject, v.getName()));
                            }
                        }else{
                            String extLabel = extNames.next();
                            projection.addElement(new ProjectionElem(extLabel, "subject"));
                            extensions.add(new ExtensionElem(subject, extLabel));
                        }
                        ValueExpr predicate = toValue(pa.getPredicate(), md);
                        if (predicate instanceof Var){
                            Var v = (Var)predicate;
                            p.addElement(new ProjectionElem(v.getName(),"predicate"));
                            if (v.getValue() != null){
                                extensions.add(new ExtensionElem(predicate, v.getName()));
                            }
                        }else{
                            String extLabel = extNames.next();
                            projection.addElement(new ProjectionElem(extLabel, "predicate"));
                            extensions.add(new ExtensionElem(predicate, extLabel));
                        }
                        ValueExpr object = toValue(pa.getObject(), md);
                        if (object instanceof Var){
                            Var v = (Var)object;
                            p.addElement(new ProjectionElem(v.getName(),"object"));
                            if (v.getValue() != null){
                                extensions.add(new ExtensionElem(object, v.getName()));
                            }
                        }else{
                            String extLabel = extNames.next();
                            projection.addElement(new ProjectionElem(extLabel, "object"));
                            extensions.add(new ExtensionElem(object, extLabel));
                        }
                        projectionElements.add(p);
                    }else{
                        blocks.addAll(((GroupBlock)bl).getBlocks());
                    }
                }
            }
        }

        if (!extensions.isEmpty()){
            tuple = new Extension(tuple, extensions);
        }
        if (!projection.getElements().isEmpty()){
            tuple = new Projection(tuple, projection);
        }else if (!projectionElements.isEmpty()){
            tuple = new MultiProjection(tuple, projectionElements);
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
    public Union visit(UnionBlock expr, QueryMetadata md) {
        List<TupleExpr> tuples = new ArrayList<TupleExpr>(expr.getBlocks().size());
        for (Block block : expr.getBlocks()){
            tuples.add(toTuple(block, md));
        }
        return new Union(tuples);
    }

    @Override
    public TupleExpr visit(GroupBlock expr, QueryMetadata md) {
        return visit((ContainerBlock)expr, md);
    }

    @Override
    public TupleExpr visit(GraphBlock expr, QueryMetadata md) {
        graphs.push(toVar(expr.getContext(), md));
        TupleExpr rv = visit((ContainerBlock)expr, md);
        graphs.pop();
        return rv;
    }

    @Override
    public TupleExpr visit(OptionalBlock expr, QueryMetadata md) {
        return visit((ContainerBlock)expr, md);
    }

    private TupleExpr visit(ContainerBlock expr, QueryMetadata md){
        TupleExpr rv = merge(expr.getBlocks(), md);
        if (expr.getFilters() != null){
            rv = filter(rv, expr.getFilters(), md);
        }
        return rv;
    }

    private TupleExpr merge(List<Block> blocks, QueryMetadata md){
        List<TupleExpr> tuples = new ArrayList<TupleExpr>(blocks.size());
        boolean asLeftJoin = false;
        for (Block block : blocks){
            if (block instanceof OptionalBlock){
                if (!tuples.isEmpty()){
                    TupleExpr right = toTuple(block, md);
                    LeftJoin lj = new LeftJoin(tuples.size() == 1 ? tuples.get(0) : new Join(tuples), right);
                    tuples = new ArrayList<TupleExpr>();
                    tuples.add(lj);
                }else{
                    asLeftJoin = true;
                    tuples.add(toTuple(block, md));
                }

            }else if (asLeftJoin){
                LeftJoin lj = new LeftJoin(toTuple(block,md), tuples.get(0));
                tuples = new ArrayList<TupleExpr>();
                tuples.add(lj);
                asLeftJoin = false;

            }else{
                tuples.add(toTuple(block, md));
            }
        }
        if (tuples.size() > 1){
            return new Join(tuples);
        }else{
            return tuples.get(0);
        }
    }

    private TupleExpr filter(TupleExpr tuple, Predicate expr, QueryMetadata md){
        ValueExpr filter = toValue(expr, md);
        if (filter != null){
            return new Filter(tuple, filter);
        }else{
            return tuple;
        }
    }

    @Override
    public TupleExpr visit(PatternBlock expr, QueryMetadata md) {
        Var subject = toVar(expr.getSubject(), md);
        Var predicate = toVar(expr.getPredicate(), md);
        Var object = toVar(expr.getObject(), md);
        StatementPattern pattern;
        if (expr.getContext() != null){
            pattern = new StatementPattern(subject, predicate, object, toVar(expr.getContext(), md));
        }else if (!graphs.isEmpty()){
            pattern = new StatementPattern(subject, predicate, object, graphs.peek());
        }else{
            pattern = new StatementPattern(subject, predicate, object);
        }

        // datatype inference (string typed literal can be replaced with untyped) via union
        if (object.getValue() != null
            && object.getValue() instanceof Literal
            && XMLSchema.STRING.equals(((Literal)object.getValue()).getDatatype())){
            Var object2 = new Var(object.getName(), dialect.getLiteral(new LIT(object.getValue().stringValue(), RDF.text)));
            return new Union(pattern, new StatementPattern(subject, predicate, object2, pattern.getContextVar()));

        }else{
            return pattern;
        }

    }

    @Override
    public Var visit(Constant<?> expr, QueryMetadata md) {
        Var var = constantToVar.get(expr);
        if (var == null){
            var = new Var(varNames.next(), dialect.getNode((NODE)expr.getConstant()));
            var.setAnonymous(true);
            constantToVar.put(expr, var);
        }
        return var;
    }

    @Override
    public Object visit(TemplateExpression<?> expr, QueryMetadata md) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(FactoryExpression<?> expr, QueryMetadata md) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueExpr visit(Operation<?> expr, QueryMetadata md) {
        Operator<?> op = expr.getOperator();
        if (op == Ops.AND){
            return new And(toValue(expr.getArg(0), md), toValue(expr.getArg(1), md));
        }else if (op == Ops.OR){
            return new Or(toValue(expr.getArg(0), md), toValue(expr.getArg(1), md));
        }else if (op == Ops.IN){
            // expand IN to OR/EQ
            BooleanBuilder builder = new BooleanBuilder();
            for (Object o : ((Constant<Collection>)expr.getArg(1)).getConstant()) {
                builder.or(ExpressionUtils.eqConst((Expression)expr.getArg(0), o));
            }
            return (ValueExpr) builder.getValue().accept(this, md);
        }else if (op == Ops.NOT){
            return new Not(toValue(expr.getArg(0), md));
        }else if (COMPARE_OPS.containsKey(op)){
            if (expr.getArg(1) instanceof SubQueryExpression<?>){
                return new CompareAll(toValue(expr.getArg(0), md), toTuple(expr.getArg(1), md), COMPARE_OPS.get(op));
            }else{
                return new Compare(toValue(expr.getArg(0), md), toValue(expr.getArg(1), md), COMPARE_OPS.get(op));
            }
        }else if (MATH_OPS.containsKey(op)){
            return new MathExpr(toValue(expr.getArg(0), md), toValue(expr.getArg(1), md), MATH_OPS.get(op));
        }else if (op == Ops.NEGATE){
            return new MathExpr(toValue(expr.getArg(0), md), toValue(new ConstantImpl<LIT>(new LIT("-1", XSD.intType)), md), MathOp.MULTIPLY);
        }else if (op == Ops.MATCHES){
            return new Regex(new Str(toValue(expr.getArg(0), md)), new Str(toValue(expr.getArg(1), md)), null);
        }else if (op == Ops.MATCHES_IC){
            return new Regex(new Str(toValue(expr.getArg(0), md)), new Str(toValue(expr.getArg(1), md)), CASE_INSENSITIVE);
        }else if (op == Ops.STRING_IS_EMPTY){
            return new Regex(new Str(toValue(expr.getArg(0), md)), "", false);
        }else if (op == Ops.IS_NULL){
            return new Not(new Bound(toVar(expr.getArg(0), md)));
        }else if (op == Ops.IS_NOT_NULL){
            return new Bound(toVar(expr.getArg(0), md));
        }else if (op == Ops.EXISTS){
            return new Exists(toTuple(expr.getArg(0), md));
        }else if (op == Ops.DELEGATE){
            return toValue(expr.getArg(0), md);
        }else if (op == Ops.STRING_CAST){
                return new Str(toValue(expr.getArg(0), md));
        }else if (op == Ops.NUMCAST){
            return new FunctionCall(toVar(expr.getArg(1), md).getValue().stringValue(), toValue(expr.getArg(0), md));
        }else if (FUNCTION_OPS.containsKey(op)){
            List<ValueExpr> args = new ArrayList<ValueExpr>(expr.getArgs().size());
            for (Expression<?> e : expr.getArgs()){
                args.add(toValue(e, md));
            }
            return new FunctionCall(FUNCTION_OPS.get(op), args);
        }else{
            throw new IllegalArgumentException(expr.toString());
        }
    }

    @Override
    public Var visit(Path<?> expr, QueryMetadata md) {
        Var var = pathToVar.get(expr);
        if (var == null){
            var = new Var(expr.toString());
            pathToVar.put(expr, var);
        }
        return var;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TupleExpr visit(SubQueryExpression<?> expr, QueryMetadata md) {
        for (Map.Entry<ParamExpression<?>, Object> entry : md.getParams().entrySet()){
            expr.getMetadata().setParam((ParamExpression)entry.getKey(), entry.getValue());
        }
        return visit(expr.getMetadata(), QueryLanguage.TUPLE);
    }

    @Override
    public Var visit(ParamExpression<?> expr, QueryMetadata md) {
        Var var = paramToVar.get(expr);
        if (var == null){
            var = new Var(expr.getName());
            if (md.getParams().containsKey(expr)){
                var.setValue(dialect.getNode((NODE)md.getParams().get(expr)));
            }
            paramToVar.put(expr, var);
        }
        return var;
    }

}
