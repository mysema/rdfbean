package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Constant;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.OperationImpl;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.PathType;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.SubQueryExpressionImpl;
import com.mysema.query.types.TemplateExpression;
import com.mysema.query.types.TemplateExpressionImpl;
import com.mysema.query.types.Templates;
import com.mysema.query.types.ToStringVisitor;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.Param;
import com.mysema.query.types.expr.Wildcard;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.Block;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.BooleanQuery;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.InferenceOptions;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QID;
import com.mysema.rdfbean.model.QLIT;
import com.mysema.rdfbean.model.QNODE;
import com.mysema.rdfbean.model.QueryOptions;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.TupleQuery;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 *
 */
public class RDFQueryBuilder implements Visitor<Object,Filters>{

    private static final Logger logger = LoggerFactory.getLogger(RDFQueryBuilder.class);

    private static final Templates TEMPLATES = new Templates(){
        {
            add(PathType.PROPERTY, "{0}_{1}");
            add(PathType.COLLECTION_ANY, "{0}");
            add(PathType.LISTVALUE_CONSTANT, "{0}_{1}");
            add(PathType.ARRAYVALUE_CONSTANT, "{0}_{1}");
            add(PathType.MAPVALUE_CONSTANT, "{0}_{1}");
        }};

    private static final Path<LIT> COUNTER = new PathImpl<LIT>(LIT.class, "counter");

    private final RDFConnection connection;

    private final Session session;

    private final Configuration configuration;

    private final Ontology ontology;

    private final QueryOptions queryOptions;

    private final InferenceOptions inferenceOptions;

    private final QueryMetadata metadata;

    private final List<Expression<?>> projection;

    private final VarNameIterator varNames = new VarNameIterator("_var_");

    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();

    private final Map<Path<?>, UID> pathToContext = new HashMap<Path<?>, UID>();

    private final Map<ParamExpression<?>, Object> params = new HashMap<ParamExpression<?>, Object>();

    private Map<Path<?>, ParamExpression<?>> pathToMapped = new HashMap<Path<?>, ParamExpression<?>>();

    private Map<Path<?>, ParamExpression<?>> pathToKnown = new HashMap<Path<?>, ParamExpression<?>>();

    public RDFQueryBuilder(RDFConnection connection,
            Session session,
            Configuration configuration,
            Ontology ontology,
            QueryMetadata metadata) {
        this.connection = connection;
        this.session = session;
        this.configuration = configuration;
        this.ontology = ontology;
        this.queryOptions = connection.getQueryOptions();
        this.inferenceOptions = connection.getInferenceOptions();
        this.metadata = metadata;
        this.projection = new ArrayList<Expression<?>>();
    }

    @SuppressWarnings("unchecked")
    RDFQueryImpl build(boolean forCount){
        RDFQueryImpl query = new RDFQueryImpl(connection);
        Filters filters = new Filters();
        //from
        for (JoinExpression join : metadata.getJoins()){
            query.where(handleRootPath((Path<?>) join.getTarget(), filters));
        }

        //where
        if (metadata.getWhere() != null){
            query.where(transform(metadata.getWhere(), filters));
        }

        //group by
        for (Expression<?> expr : metadata.getGroupBy()){
            query.groupBy(transform(expr, filters));
        }

        //having
        if (metadata.getHaving() != null){
            query.having(transform(metadata.getHaving(), filters));
        }
        filters.beginOptional();

        if (forCount){
            projection.add(queryOptions.isCountViaAggregation() ? Wildcard.count : COUNTER);
        }else{
            //order by (optional paths)
            for (OrderSpecifier<?> os : metadata.getOrderBy()){
                query.orderBy(transform(filters, os));
            }

            // limit + offset
            query.restrict(metadata.getModifiers());

            //select (optional paths);
            for (Expression<?> expr : metadata.getProjection()){
                if (expr instanceof FactoryExpression){
                    FactoryExpression<?> fe = (FactoryExpression<?>)expr;
                    for (Expression<?> e : fe.getArgs()){
                        projection.add(transform(e, filters));
                    }
                }else{
                    projection.add(transform(expr, filters));
                }

            }
        }

        filters.endOptional();
        query.where(filters.toArray());

        if (metadata.isDistinct()){
            query.distinct();
        }

        for (Map.Entry<ParamExpression<?>, Object> entry : params.entrySet()){
            query.set((Param)entry.getKey(), entry.getValue());
        }

        return query;
    }

    public BooleanQuery createBooleanQuery() {
        return build(false).createBooleanQuery();
    }

    public TupleQuery createTupleQuery(boolean forCount) {
        return build(forCount).createTupleQuery(projection.toArray(new Expression[projection.size()]));
    }

    @Nullable
    private UID getContext(Path<?> path){
        if (pathToContext.containsKey(path)){
            return pathToContext.get(path);
        }else if (path.getMetadata().getParent() != null){
            return getContext(path.getMetadata().getParent());
        }else{
            return null;
        }
    }

    private MappedPath getMappedPath(Path<?> path) {
        PathMetadata<?> md = path.getMetadata();
        if (path.getMetadata().getPathType() != PathType.PROPERTY){
            md = md.getParent().getMetadata();
        }
        MappedClass mc = configuration.getMappedClass(md.getParent().getType());
        return mc.getMappedPath(md.getElement().toString());
    }

    protected UID getTypeForDomainClass(Class<?> clazz){
        MappedClass mc = configuration.getMappedClass(clazz);
        if (mc.getUID() != null){
            return mc.getUID();
        }else{
            throw new IllegalArgumentException("Got no RDF type for " + clazz.getName());
        }
    }

    private Block handleRootPath(Path<?> path, Filters filters) {
        MappedClass mappedClass = configuration.getMappedClass(path.getType());
        UID rdfType = mappedClass.getUID();
        UID context = mappedClass.getContext();
        QID pathNode = new QID(path.toString());
        pathToMapped.put(path, pathNode);
        if (rdfType != null){
            Collection<UID> types = ontology.getSubtypes(rdfType);
            if (types.size() > 1 && inferenceOptions.subClassOf()){
                QID type = new QID(path+"_type");
                filters.add(type.in(types));
                return Blocks.pattern(pathNode, RDF.type, type);
            }else{
                if (context != null){
                    pathToContext.put(path, context);
                    return Blocks.pattern(pathNode, RDF.type, rdfType, context);
                }else{
                    return Blocks.pattern(pathNode, RDF.type, rdfType);
                }
            }
        } else {
            throw new IllegalArgumentException("No types mapped against " + path.getType().getName());
        }
    }

    private boolean inNegation(){
        int notIndex = operatorStack.lastIndexOf(Ops.NOT);
        if (notIndex > -1){
            int existsIndex = operatorStack.lastIndexOf(Ops.EXISTS);
            return notIndex > existsIndex;
        }
        return false;
    }

    private boolean inOptionalPath(){
        return operatorStack.contains(Ops.IS_NULL) ||
        operatorStack.contains(Ops.MAP_IS_EMPTY) ||
        operatorStack.contains(Ops.CASE_WHEN) ||
        (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    @Nullable
    private Expression<?> transform(Expression<?> expr, Filters filters) {
        return (Expression<?>) expr.accept(this, filters);
    }

    @SuppressWarnings("unchecked")
    private OrderSpecifier<?> transform(Filters filters, OrderSpecifier<?> os) {
        return new OrderSpecifier(os.getOrder(), transform(os.getTarget(), filters));
    }

    @Nullable
    private Predicate transform(Predicate expr, Filters filters) {
        return (Predicate) expr.accept(this, filters);
    }

    @SuppressWarnings("unchecked")
    private Operation<?> transformPathEqNeConstant(Operation<?> operation) {
        Path<?> path = (Path<?>) operation.getArg(0);
        Constant<?> constant = (Constant<?>) operation.getArg(1);
        MappedPath mappedPath = getMappedPath(path);
        // id property
        if (path.getMetadata().getPathType() == PathType.PROPERTY
            && constant.getType().equals(String.class)
            && mappedPath.getPredicatePath().isEmpty()){
            operation = PredicateOperation.create((Operator)operation.getOperator(),
                    path,
                    new ConstantImpl<ID>(session.getId(new LID(constant.toString()))));

        // localized property
        }else if (mappedPath.getMappedProperty().isLocalized()){
            Locale locale;
            if (path.getMetadata().getPathType() == PathType.PROPERTY){
                locale = session.getCurrentLocale();
            } else {
                locale = (Locale)path.getMetadata().getElement();
            }
            operation = PredicateOperation.create((Operator)operation.getOperator(),
                    path,
                    new ConstantImpl<LIT>(new LIT(constant.toString(), locale)));
        }
        return operation;
    }

    private Param<NODE> var(String var){
        return new QNODE<NODE>(NODE.class, var);
    }
    
    public Expression<?> visit(Constant<?> constant, Filters filters){
        Object javaValue = constant.getConstant();
        ConverterRegistry converter = configuration.getConverterRegistry();
        if (List.class.isAssignableFrom(constant.getType()) && ((List<?>)constant.getConstant()).isEmpty()){
            return new ConstantImpl<UID>(RDF.nil);

        }else if (NODE.class.isAssignableFrom(constant.getType())){
            return constant;

        }else if (javaValue instanceof Class<?>){
            UID datatype = converter.getDatatype((Class<?>)javaValue);
            if (datatype != null){
                return new ConstantImpl<UID>(datatype);
            }else{
                return new ConstantImpl<UID>(getTypeForDomainClass((Class<?>)javaValue));
            }

        }else if (javaValue instanceof String){
            return new ConstantImpl<LIT>(new LIT(javaValue.toString(), XSD.stringType));

        }else if (converter.supports(javaValue.getClass())){
            String label = converter.toString(javaValue);
            UID datatype = converter.getDatatype(javaValue.getClass());
            return new ConstantImpl<LIT>(new LIT(label, datatype));

        }else{
            ID id = session.getId(javaValue);
            return new ConstantImpl<ID>(id);
        }
    }

    @Override
    public Object visit(FactoryExpression<?> expr, Filters context) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Expression<?> visit(Operation<?> expr, Filters filters){
        boolean filtersInOptional = filters.inOptional();
        boolean outerOptional = inOptionalPath();
        boolean innerOptional = false;
        operatorStack.push(expr.getOperator());

        Map<Path<?>, ParamExpression<?>> origPathToMapped = null;
        Map<Path<?>, ParamExpression<?>> origPathToKnown = null;
        if (!outerOptional && inOptionalPath()){
            filters.beginOptional();
            innerOptional = true;

            origPathToMapped = pathToMapped;
            origPathToKnown = pathToKnown;
            pathToMapped = new HashMap<Path<?>, ParamExpression<?>>(pathToMapped);
            pathToKnown = new HashMap<Path<?>, ParamExpression<?>>(pathToKnown);
        }

        List<Expression<?>> args = new ArrayList<Expression<?>>(expr.getArgs().size());
        boolean leftPath = expr.getArg(0) instanceof Path<?>;
        boolean rightPath = expr.getArgs().size() > 1 ? expr.getArg(1) instanceof Path<?> : false;
        boolean leftConstant = expr.getArg(0) instanceof Constant<?>;
        boolean rightConstant = expr.getArgs().size() > 1 ? expr.getArg(1) instanceof Constant<?> : false;

        try{

            if (!queryOptions.isPreserveStringOps()){
                if (expr.getOperator() == Ops.STARTS_WITH && rightConstant){
                    expr = PredicateOperation.create(Ops.MATCHES, expr.getArg(0), new ConstantImpl(new LIT("^"+expr.getArg(1))));

                }else if (expr.getOperator() == Ops.STARTS_WITH_IC && rightConstant){
                        expr = PredicateOperation.create(Ops.MATCHES_IC, expr.getArg(0), new ConstantImpl(new LIT("^"+expr.getArg(1))));

                }else if (expr.getOperator() == Ops.ENDS_WITH && rightConstant){
                    expr = PredicateOperation.create(Ops.MATCHES, expr.getArg(0), new ConstantImpl(new LIT(expr.getArg(1) + "$")));

                }else if (expr.getOperator() == Ops.ENDS_WITH_IC && rightConstant){
                    expr = PredicateOperation.create(Ops.MATCHES_IC, expr.getArg(0), new ConstantImpl(new LIT(expr.getArg(1) + "$")));

                }else if (expr.getOperator() == Ops.STRING_CONTAINS && rightConstant){
                    expr = PredicateOperation.create(Ops.MATCHES, expr.getArg(0), new ConstantImpl(new LIT(".*" + expr.getArg(1) + ".*")));

                }else if (expr.getOperator() == Ops.STRING_CONTAINS_IC && rightConstant){
                    expr = PredicateOperation.create(Ops.MATCHES_IC, expr.getArg(0), new ConstantImpl(new LIT(".*" + expr.getArg(1) + ".*")));

                }
            }

            if (expr.getOperator() == Ops.EQ || expr.getOperator() == Ops.NE) {
                if (leftPath && rightConstant
                        && ((Path)expr.getArg(0)).getMetadata().getPathType() != PathType.VARIABLE){
                    expr = transformPathEqNeConstant(expr);
                }

                if (expr.getOperator() == Ops.EQ
                        && expr.getArg(0) instanceof Path
                        && !inOptionalPath() && !inNegation()){

                    if (expr.getArg(1) instanceof Constant){
                        ParamExpression<?> lhs = (ParamExpression<?>) transform(expr.getArg(0), filters);
                        Constant<?> rhs = (Constant<?>)transform(expr.getArg(1), filters);
                        params.put(lhs, rhs.getConstant());
                        return null;

                    }else if (expr.getArg(1) instanceof Path){
                        if (pathToMapped.containsKey(expr.getArg(0))){
                            if (!pathToMapped.containsKey(expr.getArg(1))){
                                pathToKnown.put((Path<?>)expr.getArg(1), (ParamExpression<?>)transform(expr.getArg(0), filters));
                                transform(expr.getArg(1), filters);
                                return null;
                            }
                        }else{
                            pathToKnown.put((Path<?>)expr.getArg(0), (ParamExpression<?>)transform(expr.getArg(1), filters));
                            transform(expr.getArg(0), filters);
                            return null;
                        }


                    }

                }

            }else if (expr.getOperator() == Ops.AND){
                Predicate lhs, rhs;
                if (expr.getArg(0) instanceof Operation && ((Operation)expr.getArg(0)).getOperator() == Ops.OR){
                    lhs = (Predicate) transform(expr.getArg(1), filters);
                    rhs = (Predicate) transform(expr.getArg(0), filters);
                }else{
                    lhs = (Predicate) transform(expr.getArg(0), filters);
                    rhs = (Predicate) transform(expr.getArg(1), filters);
                }
                return lhs == null ? rhs : (rhs == null ? lhs : ExpressionUtils.and(lhs, rhs));

            }else if (expr.getOperator() == Ops.IN){
                Expression<?> lhs = expr.getArg(0);
                if (leftPath){
                    lhs = transform(lhs, filters);
                }
                if ((leftPath && rightPath) || (leftConstant && rightPath)){
                    expr = (Operation)ExpressionUtils.eq(lhs, (Expression)expr.getArg(1));
                }else if (leftPath && rightConstant){
                    Collection col = (Collection)((Constant)expr.getArg(1)).getConstant();
                    if (!col.isEmpty()){
                        BooleanBuilder builder = new BooleanBuilder();
                        for (Object o : col){
                            builder.or(ExpressionUtils.eq(lhs, new ConstantImpl(o)));
                        }
                        expr = (Operation)builder.getValue();
                    }else{
                        throw new IllegalArgumentException(expr.toString());
                    }

                }else{
                    throw new IllegalArgumentException(expr.toString());
                }

            }else if (expr.getOperator() == Ops.BETWEEN){
                Operator<Boolean> first = Ops.GOE, second = Ops.LOE;
                expr = (Operation<?>) ExpressionUtils.and(
                        PredicateOperation.create(first, expr.getArg(0), expr.getArg(1)),
                        PredicateOperation.create(second, expr.getArg(0), expr.getArg(2)));

            }else if (expr.getOperator() == Ops.ORDINAL){
                Param<?> path = (Param<?>) transform(expr.getArg(0), filters);
                Param<?> ordinalPath = new QLIT(path.getName()+"_ordinal");
                filters.add(Blocks.pattern(path, CORE.enumOrdinal, ordinalPath));
                return ordinalPath;

            }else if (expr.getOperator() == Ops.INSTANCE_OF){
                Param<?> path = (Param<?>) transform(expr.getArg(0), filters);
                Constant<?> type = (Constant<?>) transform(expr.getArg(1), filters);
                Block pattern = Blocks.pattern(path, RDF.type, type);
                if (inNegation() || inOptionalPath()){
                    return pattern.exists();
                }else{
                    filters.add(pattern);
                    return null;
                }

            }else if (expr.getOperator() == Ops.COL_IS_EMPTY){
                expr = (Operation<?>) ExpressionUtils.eq(expr.getArg(0), new ConstantImpl(RDF.nil));

            }else if (expr.getOperator() == Ops.CONTAINS_KEY){
                Path<?> path = (Path<?>) expr.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();
                Expression<?> key = transform(expr.getArg(1), filters);
                Block pattern = Blocks.pattern(transform(path, filters), mappedProperty.getKeyPredicate(), key);
                if (inNegation() || inOptionalPath()){
                    return pattern.exists();
                }else{
                    filters.add(pattern);
                    return null;
                }

            }else if (expr.getOperator() == Ops.CONTAINS_VALUE){
                Path<?> path = (Path<?>) expr.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();

                if (mappedProperty.getValuePredicate() != null){
                    Expression<?> value = transform(expr.getArg(1), filters);
                    Block pattern = Blocks.pattern(transform(path, filters), mappedProperty.getValuePredicate(), value);
                    if (inNegation() || inOptionalPath()){
                        return pattern.exists();
                    }else{
                        filters.add(pattern);
                        return null;
                    }

                }else{
                    expr = (Operation<?>) ExpressionUtils.eq((Path)path, expr.getArg(1));
                }

            }else if (expr.getOperator() == Ops.MAP_IS_EMPTY){
                return PredicateOperation.create(Ops.IS_NULL, transform(expr.getArg(0), filters));

            }else if (expr.getOperator() == Ops.COALESCE){
                List<Expression<?>> elements = new ArrayList<Expression<?>>();
                expr = new OperationImpl(expr.getType(), Ops.COALESCE, 
                        ImmutableList.copyOf(transformList(expr.getArg(0), elements)));
            }

            if (operatorStack.peek() != expr.getOperator()){
                operatorStack.pop();
                operatorStack.push(expr.getOperator());
            }

            for (Expression<?> arg : expr.getArgs()){
                Expression<?> transformed = transform(arg, filters);
                if (transformed != null){
                    args.add(transformed);
                }else{
                    logger.error(arg + " skipped");
                }
            }
        }finally{
            operatorStack.pop();
            if (!outerOptional && innerOptional){
                if (!filtersInOptional){
                    filters.endOptional();
                }
                pathToMapped = origPathToMapped;
                pathToKnown = origPathToKnown;
            }
        }

        if (expr.getType().equals(Boolean.class)){
            return new PredicateOperation((Operator)expr.getOperator(), ImmutableList.copyOf(args));
        }else{
            return new OperationImpl(expr.getType(),expr.getOperator(), ImmutableList.copyOf(args));
        }

    }

    private List<Expression<?>> transformList(Expression<?> expr, List<Expression<?>> elements) {
        if (expr instanceof Operation<?> && ((Operation<?>)expr).getOperator() == Ops.LIST){
            Operation<?> list = (Operation<?>)expr;
            transformList(list.getArg(0), elements);
            elements.add(list.getArg(1));
        }else{
            elements.add(expr);
        }
        return elements;
    }

    @Override
    public Object visit(ParamExpression<?> expr, Filters context) {
        if (NODE.class.isAssignableFrom(expr.getType())){
            return expr;
        }else{
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Expression<?> visit(Path<?> path, Filters filters){
        if (pathToMapped.containsKey(path)){
            return pathToMapped.get(path);

        }else if (path.getMetadata().getParent() != null){
            PathMetadata<?> md = path.getMetadata();
            PathType pathType = md.getPathType();
            ParamExpression<?> parent = (ParamExpression<?>) visit(md.getParent(), filters);
            ParamExpression<?> pathNode = null;
            ParamExpression<?> rdfPath = pathToKnown.get(path);
            UID context = getContext(path);

            if (pathType.equals(PathType.PROPERTY)){
                MappedPath mappedPath = getMappedPath(path);
                List<MappedPredicate> predPath = mappedPath.getPredicatePath();
                if (predPath.size() > 0){
                    MappedPredicate last = predPath.get(predPath.size()-1);
                    if (last.includeInferred()){
                        pathToContext.put(path, context = null);
                    }else if (last.getContext() != null){
                        pathToContext.put(path, context = last.getContext());
                    }else if (configuration.isMapped(path.getType())){
                        MappedClass mappedClass = configuration.getMappedClass(path.getType());
                        if (mappedClass.getClass() != null){
                            pathToContext.put(path, mappedClass.getContext());
                        }
                    }
                    for (int i = 0; i < predPath.size(); i++){
                        MappedPredicate pred = predPath.get(i);
                        UID c = pred.getContext() != null ? pred.getContext() : context;
                        if (rdfPath != null && (i == predPath.size()-1)){
                            pathNode = rdfPath;
                        }else if (i == predPath.size() -1){
                            String var = path.accept(ToStringVisitor.DEFAULT, TEMPLATES);
                            pathNode = var(var);
                            varNames.disallow(var);
                        }else{
                            pathNode = var(varNames.next());
                        }
                        if (!pred.inv()){
                            filters.add(Blocks.pattern(parent, pred.getUID(), pathNode, c));
                        }else{
                            filters.add(Blocks.pattern(pathNode, pred.getUID(), parent, c));
                        }
                        parent = pathNode;
                    }

                }else{
                    pathNode = parent;
                }

            }else if (pathType.equals(PathType.COLLECTION_ANY)){
                return visit(path.getMetadata().getParent(), filters);

            }else if (pathType.equals(PathType.ARRAYVALUE)
                    || pathType.equals(PathType.LISTVALUE)){
                throw new UnsupportedOperationException(pathType + " not supported!");

            }else if (pathType.equals(PathType.ARRAYVALUE)
                    || pathType.equals(PathType.LISTVALUE_CONSTANT)){
                int index = ((Integer)md.getElement()).intValue();
                for (int i = 0; i < index; i++){
                    pathNode = var(varNames.next());
                    filters.add(Blocks.pattern(parent, RDF.rest, pathNode, context));
                    parent = pathNode;
                }
                pathNode = var(varNames.next());
                filters.add(Blocks.pattern(parent, RDF.first, pathNode, context));

            }else if (pathType.equals(PathType.MAPVALUE)
                    || pathType.equals(PathType.MAPVALUE_CONSTANT)){
                MappedPath mappedPath = getMappedPath(md.getParent());
                MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                if (!mappedProperty.isLocalized()){
                    Object element = path.getMetadata().getElement();
                    Expression<?> expr = transform(element instanceof Expression ? 
                            (Expression)element : new ConstantImpl(element), filters);
                    filters.add(Blocks.pattern(parent, mappedProperty.getKeyPredicate(), expr, context));

                    if (mappedProperty.getValuePredicate() != null){
                        pathNode = var(varNames.next());
                        filters.add(Blocks.pattern(parent, mappedProperty.getValuePredicate(), pathNode, context));
                    }else{
                        pathNode = parent;
                    }

                }else{
                    pathNode = parent;
                }

            }

            pathToMapped.put(path, pathNode);
            return pathNode;

        }else{
            throw new IllegalArgumentException("Undeclared path " + path);
        }

    }

    public Expression<?> visit(SubQueryExpression<?> expr, Filters filters) {
        QueryMetadata md = expr.getMetadata();
        Filters f = new Filters();

        // from
        for (JoinExpression join : md.getJoins()){
            f.add(handleRootPath((Path<?>) join.getTarget(), f));
        }
        // where
        if (md.getWhere() != null){
            f.add(transform(md.getWhere(), f));
        }
        // select
        if (!md.getProjection().isEmpty()){
            QueryMetadata rv = new DefaultQueryMetadata().noValidate();
            for (Expression<?> e : md.getProjection()){
                rv.addProjection(transform(e, f));
            }
            rv.addWhere(f.asBlock());
            return new SubQueryExpressionImpl<Object>(Object.class, rv);
        }else{
            return f.asBlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Expression<?> visit(TemplateExpression<?> template, Filters filters){
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        for (Object arg : template.getArgs()){
            if (arg instanceof Expression) {
                arg = transform((Expression)arg, filters);                    
            }
            if (arg != null){
                builder.add(arg);
            }
            
        }
        if (template.getType().equals(Boolean.class)){
            return new BooleanTemplate(template.getTemplate(), builder.build());
        }else{
            return new TemplateExpressionImpl(template.getType(), template.getTemplate(), builder.build());
        }
    }

}
