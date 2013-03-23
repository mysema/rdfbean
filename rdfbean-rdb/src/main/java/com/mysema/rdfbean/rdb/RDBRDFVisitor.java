package com.mysema.rdfbean.rdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.QueryMetadata;
import com.mysema.query.sql.SQLCommonQuery;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.OperationImpl;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.TemplateExpressionImpl;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.ContainerBlock;
import com.mysema.rdfbean.model.GraphBlock;
import com.mysema.rdfbean.model.GroupBlock;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.OptionalBlock;
import com.mysema.rdfbean.model.PatternBlock;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFVisitor;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.UnionBlock;
import com.mysema.rdfbean.query.VarNameIterator;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;

/**
 * @author tiwe
 * 
 */
public class RDBRDFVisitor implements RDFVisitor<Object, QueryMetadata> {

    @SuppressWarnings("unchecked")
    private static final Set<Operator<?>> MATH = new HashSet<Operator<?>>(Arrays.asList(
            Ops.LT, Ops.GT, Ops.LOE, Ops.GOE,
            Ops.MathOps.ABS, Ops.ADD, Ops.SUB, Ops.MULT, Ops.MOD, Ops.DIV, Ops.AggOps.AVG_AGG, Ops.AggOps.SUM_AGG));

    @SuppressWarnings("unchecked")
    private static final Set<Operator<?>> DATE = new HashSet<Operator<?>>(Arrays.asList(
            Ops.DateTimeOps.DAY_OF_MONTH, Ops.DateTimeOps.DAY_OF_WEEK, Ops.DateTimeOps.DAY_OF_YEAR, Ops.DateTimeOps.HOUR,
            Ops.DateTimeOps.MILLISECOND, Ops.DateTimeOps.MINUTE, Ops.DateTimeOps.MONTH, Ops.DateTimeOps.SECOND, Ops.DateTimeOps.WEEK,
            Ops.DateTimeOps.YEAR, Ops.DateTimeOps.YEAR_MONTH));

    private final RDBContext context;

    private final Set<Expression<?>> namedExpressions = new HashSet<Expression<?>>();

    private Map<Expression<?>, Expression<?>> exprToSymbol = new HashMap<Expression<?>, Expression<?>>();

    private Map<Expression<?>, Path<Long>> exprToMapped = new HashMap<Expression<?>, Path<Long>>();

    private final Set<Expression<?>> resources = new HashSet<Expression<?>>();

    private final Set<QSymbol> numericSymbols = new HashSet<QSymbol>();

    private final Set<QSymbol> dateTimeSymbols = new HashSet<QSymbol>();

    private final Stack<Expression<UID>> graphs = new Stack<Expression<UID>>();

    private final Stack<Operator<?>> operators = new Stack<Operator<?>>();

    private final Function<Long, NODE> transformer;

    private final VarNameIterator stmts = new VarNameIterator("stmts");

    private final VarNameIterator symbols = new VarNameIterator("symbols");

    private SQLCommonQuery<?> query;

    private boolean inOptional;

    private boolean firstSource = true;

    private boolean asLiteral = false;

    public RDBRDFVisitor(RDBContext context, Function<Long, NODE> transformer) {
        this.context = context;
        this.transformer = transformer;
    }

    private Long getId(NODE node) {
        return context.getNodeId(node);
    }

    private Long getId(Constant<NODE> constant) {
        return context.getNodeId(constant.getConstant());
    }

    private Expression<?> handle(Expression<?> expr, QueryMetadata md) {
        return (Expression<?>) expr.accept(this, md);
    }

    private boolean needsSymbolResolving(Operation<?> op) {
        if (Ops.equalsOps.contains(op.getOperator())
                || Ops.notEqualsOps.contains(op.getOperator())
                || op.getOperator() == Ops.IN
                || op.getOperator() == Ops.ORDINAL) {
            for (Expression<?> arg : op.getArgs()) {
                if (!(arg instanceof Path<?>)
                        && !(arg instanceof ParamExpression<?>)
                        && !(arg instanceof Constant<?>)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(QueryMetadata md, QueryLanguage<?, ?> queryType) {
        query = context.createQuery();
        // FIXME
        boolean asCount = md.getProjection().size() == 1 && md.getProjection().get(0).toString().equals("count(*)");

        // where
        handle(md.getWhere(), md);

        // group by
        for (Expression<?> gb : md.getGroupBy()) {
            query.groupBy(handle(gb, md));
        }

        // having
        if (md.getHaving() != null) {
            query.having((Predicate) handle(md.getHaving(), md));
        }

        if (!asCount) {
            // order by
            asLiteral = true;
            for (OrderSpecifier<?> order : md.getOrderBy()) {
                query.orderBy(new OrderSpecifier(order.getOrder(), handle(order.getTarget(), md)));
            }
            asLiteral = false;

            // limit + offset
            query.restrict(md.getModifiers());

            // distinct
            if (md.isDistinct()) {
                query.distinct();
            }
        }

        // select
        if (queryType.equals(QueryLanguage.TUPLE)) {
            List<String> variables = new ArrayList<String>();
            List<Expression<?>> pr = new ArrayList<Expression<?>>();

            Collection<? extends Expression<?>> projection = md.getProjection();
            if (projection.isEmpty()) {
                projection = namedExpressions;
            }
            for (Expression<?> expr : projection) {
                if (expr instanceof ParamExpression) {
                    variables.add(((ParamExpression) expr).getName());
                } else {
                    variables.add(expr.toString());
                }
                if (resources.contains(expr) || ID.class.isAssignableFrom(expr.getType())) {
                    boolean asLit = asLiteral;
                    asLiteral = true;
                    pr.add(handle(expr, md));
                    asLiteral = asLit;
                } else {
                    pr.add(handle(expr, md));
                }
            }

            return new TupleQueryImpl((SQLQuery) query, context.getConverters(), variables, pr, transformer);

            // construct
        } else if (queryType.equals(QueryLanguage.GRAPH)) {
            // TODO : add also support for larger patterns
            if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof PatternBlock) {
                List<Expression<?>> pr = new ArrayList<Expression<?>>();
                PatternBlock pattern = (PatternBlock) md.getProjection().get(0);

                for (Expression<?> expr : Arrays.asList(pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), pattern.getContext())) {
                    if (expr != null && !(expr instanceof Constant)) {
                        if (resources.contains(expr) || ID.class.isAssignableFrom(expr.getType())) {
                            boolean asLit = asLiteral;
                            asLiteral = true;
                            pr.add(handle(expr, md));
                            asLiteral = asLit;
                        } else {
                            pr.add(handle(expr, md));
                        }
                    }
                }
                return new GraphQueryImpl((SQLQuery) query, pattern, pr, transformer);

            } else {
                throw new UnsupportedOperationException();
            }

            // ask
        } else if (queryType.equals(QueryLanguage.BOOLEAN)) {
            return new BooleanQueryImpl((SQLQuery) query);

        } else {
            throw new UnsupportedOperationException();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Constant<?> visit(Constant<?> constant, QueryMetadata context) {
        if (NODE.class.isAssignableFrom(constant.getType())) {
            NODE node = (NODE) constant.getConstant();
            if (asLiteral) {
                return ConstantImpl.create(node.getValue());
            } else {
                return ConstantImpl.create(getId(node));
            }

        } else if (Collection.class.isAssignableFrom(constant.getType())) {
            Collection<?> collection = (Collection<?>) constant.getConstant();
            List<Object> rv = new ArrayList<Object>(collection.size());
            for (Object o : collection) {
                NODE node = (NODE) o;
                if (asLiteral) {
                    rv.add(node.getValue());
                } else {
                    rv.add(getId(node));
                }
            }
            return new ConstantImpl<List>(List.class, rv);

        } else if (String.class.equals(constant.getType())) {
            if (asLiteral) {
                return ConstantImpl.create(constant.getConstant().toString());
            } else {
                return ConstantImpl.create(getId(new LIT(constant.getConstant().toString())));
            }

        } else if (ConverterRegistryImpl.DEFAULT.supports(constant.getType())) {
            String value = ConverterRegistryImpl.DEFAULT.toString(constant.getConstant());
            if (asLiteral) {
                return ConstantImpl.create(value);
            } else {
                UID datatype = ConverterRegistryImpl.DEFAULT.getDatatype(constant.getType());
                return ConstantImpl.create(getId(new LIT(value, datatype)));
            }

        } else {
            throw new IllegalArgumentException(constant.toString());
        }
    }

    @Override
    public Object visit(FactoryExpression<?> expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(GraphBlock expr, QueryMetadata context) {
        resources.add(expr.getContext());
        graphs.push(expr.getContext());
        visit((ContainerBlock) expr, context);
        graphs.pop();
        return null;
    }

    @Override
    public Object visit(GroupBlock expr, QueryMetadata context) {
        return visit((ContainerBlock) expr, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(Operation<?> expr, QueryMetadata context) {
        List<Expression<?>> args = new ArrayList<Expression<?>>(expr.getArgs().size());
        boolean asLit = asLiteral;
        asLiteral = needsSymbolResolving(expr);

        try {
            operators.push(expr.getOperator());

            if (expr.getOperator() == Ops.NUMCAST) {
                args.add(handle(expr.getArg(0), context));
                UID datatype = (UID) ((Constant) expr.getArg(1)).getConstant();
                args.add(new ConstantImpl<Class>(this.context.getConverters().getClass(datatype)));
            } else {
                for (Expression<?> arg : expr.getArgs()) {
                    args.add(handle(arg, context));
                }
            }
        } finally {
            operators.pop();
        }

        asLiteral = asLit;
        if (expr.getType().equals(Boolean.class)) {
            return new PredicateOperation((Operator) expr.getOperator(), ImmutableList.copyOf(args));
        } else {
            return new OperationImpl(expr.getType(), expr.getOperator(), ImmutableList.copyOf(args));
        }
    }

    @Override
    public Object visit(OptionalBlock expr, QueryMetadata context) {
        boolean inOpt = inOptional;
        inOptional = true;
        visit((ContainerBlock) expr, context);
        inOptional = inOpt;
        return null;
    }

    @Nullable
    private Object visit(ContainerBlock expr, QueryMetadata context) {
        for (Block block : expr.getBlocks()) {
            handle(block, context);
        }
        if (expr.getFilters() != null) {
            query.where((Predicate) handle(expr.getFilters(), context));
        }
        return null;
    }

    @Override
    public Expression<?> visit(ParamExpression<?> expr, QueryMetadata context) {
        namedExpressions.add(expr);
        return visitPathOrParam(expr, expr.getName(), context);
    }

    @Override
    public Expression<?> visit(Path<?> expr, QueryMetadata context) {
        namedExpressions.add(expr);
        return visitPathOrParam(expr, expr.toString(), context);
    }

    private Expression<?> visitPathOrParam(Expression<?> expr, String exprToString, QueryMetadata context) {
        if (asLiteral) {
            if (exprToSymbol.containsKey(expr)) {
                return exprToSymbol.get(expr);
            } else {
                QSymbol symbol = new QSymbol(symbols.next());
                query.leftJoin(symbol).on(symbol.id.eq(exprToMapped.get(expr)));
                Expression<?> lexical = symbol.lexical;
                if (inMathOperation()) {
                    lexical = symbol.floatval;
                    numericSymbols.add(symbol);
                } else if (inDateOperation()) {
                    lexical = symbol.datetimeval;
                    dateTimeSymbols.add(symbol);
                } else if (numericSymbols.contains(symbol)) {
                    lexical = symbol.floatval;
                } else if (dateTimeSymbols.contains(symbol)) {
                    lexical = symbol.datetimeval;
                }

                exprToSymbol.put(expr, lexical);
                return lexical;
            }
        } else {
            return exprToMapped.get(expr);
        }
    }

    private boolean inMathOperation() {
        for (Operator<?> op : operators) {
            if (MATH.contains(op)) {
                return true;
            }
        }
        return false;
    }

    private boolean inDateOperation() {
        for (Operator<?> op : operators) {
            if (DATE.contains(op)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object visit(PatternBlock expr, QueryMetadata context) {
        QStatement stmt = new QStatement(stmts.next());
        BooleanBuilder filters = new BooleanBuilder();
        resources.add(expr.getSubject());
        resources.add(expr.getPredicate());
        if (ID.class.isAssignableFrom(expr.getObject().getType())) {
            resources.add(expr.getObject());
        }
        if (expr.getContext() != null) {
            resources.add(expr.getContext());
        }
        if (isNamed(expr.getSubject())) {
            namedExpressions.add(expr.getSubject());
        }
        if (isNamed(expr.getPredicate())) {
            namedExpressions.add(expr.getPredicate());
        }
        if (isNamed(expr.getObject())) {
            namedExpressions.add(expr.getObject());
        }
        if (isNamed(expr.getContext())) {
            namedExpressions.add(expr.getContext());
        }
        filters.and(visitPatternElement(context, stmt.subject, expr.getSubject()));
        filters.and(visitPatternElement(context, stmt.predicate, expr.getPredicate()));
        filters.and(visitPatternElement(context, stmt.object, expr.getObject()));
        Expression<UID> c = expr.getContext();
        if (c == null && !graphs.isEmpty()) {
            c = graphs.peek();
        }
        if (c != null) {
            filters.and(visitPatternElement(context, stmt.model, c));
        }

        if (firstSource) {
            query.from(stmt).where(filters.getValue());
        } else if (inOptional) {
            query.leftJoin(stmt).on(filters.getValue());
        } else {
            query.innerJoin(stmt).on(filters.getValue());
        }
        firstSource = false;
        return null;
    }

    private boolean isNamed(Expression<?> expr) {
        return expr instanceof Path<?> || expr instanceof ParamExpression<?>;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Predicate visitPatternElement(QueryMetadata context, NumberPath<Long> path, Expression<? extends NODE> s) {
        if (s instanceof Constant<?>) {
            return path.eq(getId((Constant) s));
        } else {
            if (exprToMapped.containsKey(s)) {
                return path.eq(exprToMapped.get(s));
            } else {
                exprToMapped.put(s, path);
                if (s instanceof ParamExpression<?>) {
                    Object constant = context.getParams().get(s);
                    if (constant != null) {
                        return path.eq(getId((NODE) constant));
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
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

        // copy bindings from parent
        for (Map.Entry<ParamExpression<?>, Object> entry : context.getParams().entrySet()) {
            expr.getMetadata().setParam((ParamExpression) entry.getKey(), entry.getValue());
        }
        QueryMetadata md = expr.getMetadata();

        // where
        handle(md.getWhere(), md);

        // group by
        for (Expression<?> gb : md.getGroupBy()) {
            query.groupBy(handle(gb, md));
        }

        // having
        if (md.getHaving() != null) {
            query.having((Predicate) handle(md.getHaving(), md));
        }

        // order by
        boolean asLit = asLiteral;
        asLiteral = true;
        for (OrderSpecifier<?> order : md.getOrderBy()) {
            query.orderBy(new OrderSpecifier(order.getOrder(), handle(order.getTarget(), md)));
        }
        asLiteral = asLit;

        // limit + offset
        query.restrict(md.getModifiers());

        // distinct
        if (md.isDistinct()) {
            query.distinct();
        }

        List<Expression<?>> projection = new ArrayList<Expression<?>>();
        for (Expression<?> e : md.getProjection()) {
            projection.add(handle(e, md));
        }
        SubQueryExpression<?> sqe = ((SQLSubQuery) query).list(projection.toArray(new Expression[projection.size()]));

        firstSource = firstS;
        query = q;
        exprToMapped = exprToM;
        exprToSymbol = exprToS;

        return sqe;
    }

    @Override
    public Object visit(TemplateExpression<?> expr, QueryMetadata context) {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (Object arg : expr.getArgs()) {
            if (arg instanceof Expression) {
                builder.add(handle((Expression) arg, context));
            } else {
                builder.add(arg);
            }

        }
        if (expr.getType().equals(Boolean.class)) {
            return new BooleanTemplate(expr.getTemplate(), builder.build());
        } else {
            return new TemplateExpressionImpl<Object>(expr.getType(), expr.getTemplate(), builder.build());
        }
    }

    @Override
    public Object visit(UnionBlock expr, QueryMetadata context) {
        throw new UnsupportedOperationException();
    }

}
