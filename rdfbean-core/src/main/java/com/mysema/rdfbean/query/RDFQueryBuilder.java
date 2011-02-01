package com.mysema.rdfbean.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinExpression;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.*;
import com.mysema.query.types.Operation;
import com.mysema.query.types.expr.BooleanOperation;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * @author tiwe
 *
 */
public class RDFQueryBuilder implements Visitor<Object,Filters>{
    
    private static final Templates TEMPLATES = new Templates(){
        {
            add(PathType.PROPERTY, "{0}_{1}");
            add(PathType.COLLECTION_ANY, "{0}");
            add(PathType.LISTVALUE_CONSTANT, "{0}_{1}");
            add(PathType.ARRAYVALUE_CONSTANT, "{0}_{1}");
            add(PathType.MAPVALUE_CONSTANT, "{0}_{1}");
        }};    
    
    private final RDFConnection connection;
    
    private final Session session;
    
    private final Configuration configuration;
    
    private final QueryMetadata metadata;
    
    private final List<Expression<?>> projection;
    
    private final VarNameIterator varNames = new VarNameIterator("_var_");
    
    private final Stack<Operator<?>> operatorStack = new Stack<Operator<?>>();
    
    private final Map<Path<?>, UID> pathToContext = new HashMap<Path<?>, UID>();
    
    private Map<Path<?>, Path<?>> pathToMapped = new HashMap<Path<?>, Path<?>>();
    
    public RDFQueryBuilder(RDFConnection connection,
            Session session,
            Configuration configuration, 
            QueryMetadata metadata) {
        this.connection = connection;
        this.session = session;
        this.configuration = configuration;
        this.metadata = metadata;
        this.projection = new ArrayList<Expression<?>>();
    }

    RDFQueryImpl build(boolean forCount){
        RDFQueryImpl query = new RDFQueryImpl(connection);
        Filters filters = new Filters();
        //from 
        for (JoinExpression join : metadata.getJoins()){
            query.where(handleRootPath((Path<?>) join.getTarget()));
        }        
        
        //where
        if (metadata.getWhere() != null){
            query.where(transform(filters, metadata.getWhere()));
        }        
        
        //group by
        for (Expression<?> expr : metadata.getGroupBy()){
            query.groupBy(transform(expr, filters));
        }        
        
        //having
        if (metadata.getHaving() != null){
            query.having(transform(filters, metadata.getHaving()));
        }                
        filters.beginOptional();
                        
        //order by (optional paths)
        for (OrderSpecifier<?> os : metadata.getOrderBy()){
            query.orderBy(transform(filters, os));
        }        
        
        if (forCount){
            // TODO : alternatively COUNT(*), if supported
            projection.add(new PathImpl<LIT>(LIT.class, "counter"));
        }else{
            // limit + offset
            query.restrict(metadata.getModifiers());
                    
            //select (optional paths);
            for (Expression<?> expr : metadata.getProjection()){
                projection.add(transform(expr, filters));
            }
        }
        
        filters.endOptional();
        query.where(filters.toArray());
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
        return mc.getMappedPath(md.getExpression().toString());           
    }

    protected UID getTypeForDomainClass(Class<?> clazz){        
        MappedClass mc = configuration.getMappedClass(clazz);
        if (mc.getUID() != null){
            return mc.getUID();
        }else{
            throw new IllegalArgumentException("Got no RDF type for " + clazz.getName());
        }
    }
    
    private Block handleRootPath(Path<?> path) {
        MappedClass mappedClass = configuration.getMappedClass(path.getType());
        UID rdfType = mappedClass.getUID();
        UID context = mappedClass.getContext();
        pathToMapped.put(path, path);
        if (rdfType != null){
            if (context != null){
                pathToContext.put(path, context);
                return Blocks.pattern(path, RDF.type, rdfType, context);
            }else{
                return Blocks.pattern(path, RDF.type, rdfType);
            }
        } else {
            throw new IllegalArgumentException("No types mapped against " + path.getType().getName());
        }        
    }
    
    public boolean inNegation(){
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
        (operatorStack.contains(Ops.OR) && operatorStack.peek() != Ops.OR);
    }
    
    @Nullable
    private Expression<?> transform(Expression<?> expr, Filters filters) {
        return (Expression<?>) expr.accept(this, filters);   
    }
    
    @SuppressWarnings("unchecked")
    private OrderSpecifier<?> transform(Filters filters, OrderSpecifier<?> os) {
        Expression<?> expr = transform(os.getTarget(), filters);
        return new OrderSpecifier(os.getOrder(), expr);
    }
    
    @Nullable
    private Predicate transform(Filters filters, Predicate where) {
        return (Predicate) transform((Expression<?>)where, filters);
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
            operation = new PredicateOperation((Operator)operation.getOperator(), 
                    path, 
                    new ConstantImpl<ID>(session.getId(new LID(constant.toString()))));
            
        // localized property
        }else if (mappedPath.getMappedProperty().isLocalized()){
            Locale locale;
            if (path.getMetadata().getPathType() == PathType.PROPERTY){
                locale = session.getCurrentLocale();
            }else{
                locale = (Locale)((Constant<?>)path.getMetadata().getExpression()).getConstant();
            } 
            operation = new PredicateOperation((Operator)operation.getOperator(), 
                    path, 
                    new ConstantImpl<LIT>(new LIT(constant.toString(), locale)));
        }
        return operation;
    }

    private Path<NODE> var(String var){
        return new PathImpl<NODE>(NODE.class, var);
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
    public Expression<?> visit(Operation<?> operation, Filters filters){
        boolean filtersInOptional = filters.inOptional();
        boolean outerOptional = inOptionalPath();
        boolean innerOptional = false;
        operatorStack.push(operation.getOperator());
        Map<Path<?>, Path<?>> origPathToMapped = null;
        if (!outerOptional && inOptionalPath()){
            filters.beginOptional();
            innerOptional = true;
            
            origPathToMapped = pathToMapped;
            pathToMapped = new HashMap<Path<?>, Path<?>>(pathToMapped);
        }
        
        List<Expression<?>> args = new ArrayList<Expression<?>>(operation.getArgs().size());
        boolean leftPath = operation.getArg(0) instanceof Path<?>;
        boolean rightPath = operation.getArgs().size() > 1 ? operation.getArg(1) instanceof Path<?> : false;
        boolean leftConstant = operation.getArg(0) instanceof Constant<?>;
        boolean rightConstant = operation.getArgs().size() > 1 ? operation.getArg(1) instanceof Constant<?> : false;
        
        try{
            if (operation.getOperator() == Ops.EQ_OBJECT || operation.getOperator() == Ops.NE_OBJECT){
                if (leftPath 
                        && rightConstant 
                        && ((Path)operation.getArg(0)).getMetadata().getPathType() != PathType.VARIABLE){
                    operation = transformPathEqNeConstant(operation);
                }
                
            }else if (operation.getOperator() == Ops.STARTS_WITH && rightConstant){    
                operation = new PredicateOperation(Ops.MATCHES, 
                        operation.getArg(0), new ConstantImpl(new LIT("^"+operation.getArg(1))));
            
            }else if (operation.getOperator() == Ops.ENDS_WITH && rightConstant){    
                operation = new PredicateOperation(Ops.MATCHES, 
                        operation.getArg(0), new ConstantImpl(new LIT(operation.getArg(1) + "$")));
            
            }else if (operation.getOperator() == Ops.STRING_CONTAINS && rightConstant){    
                operation = new PredicateOperation(Ops.MATCHES, 
                        operation.getArg(0), new ConstantImpl(new LIT(".*" + operation.getArg(1) + ".*")));
                                
            }else if (operation.getOperator() == Ops.AND){
                Predicate lhs = (Predicate) transform(operation.getArg(0), filters);
                Predicate rhs = (Predicate) transform(operation.getArg(1), filters);
                return lhs == null ? rhs : (rhs == null ? lhs : ExpressionUtils.and(lhs, rhs));
                    
            }else if (operation.getOperator() == Ops.IN){
                if ((leftPath && rightPath) || (leftConstant && rightPath)){
                    operation = (Operation)ExpressionUtils.eq(operation.getArg(0), (Expression)operation.getArg(1));
                }else if (leftPath && rightConstant){
                    Collection col = (Collection)((Constant)operation.getArg(1)).getConstant();
                    if (!col.isEmpty()){
                        BooleanBuilder builder = new BooleanBuilder();
                        for (Object o : col){
                            builder.or(ExpressionUtils.eq(operation.getArg(0), new ConstantImpl(o)));
                        }
                        operation = (Operation)builder.getValue();    
                    }else{
                        throw new IllegalArgumentException(operation.toString());
                    }
                    
                }else{
                    throw new IllegalArgumentException(operation.toString());
                }
                
            }else if (operation.getOperator() == Ops.BETWEEN){
                operation = (Operation<?>) ExpressionUtils.and(
                        new PredicateOperation(Ops.GOE, operation.getArg(0), operation.getArg(1)), 
                        new PredicateOperation(Ops.LOE, operation.getArg(0), operation.getArg(2)));
                
            }else if (operation.getOperator() == Ops.ORDINAL){
                Path<?> path = (Path<?>) transform(operation.getArg(0), filters);
                Path<?> ordinalPath = new PathImpl<LIT>(LIT.class, path.toString()+"_ordinal");
                filters.add(Blocks.pattern(path, CORE.enumOrdinal, ordinalPath));
                return ordinalPath;
                
            }else if (operation.getOperator() == Ops.INSTANCE_OF){
                Path<?> path = (Path<?>) transform(operation.getArg(0), filters);
                Constant<?> type = (Constant<?>) transform(operation.getArg(1), filters);
                Block pattern = Blocks.pattern(path, RDF.type, type); 
                if (inNegation() || inOptionalPath()){
                    return pattern.exists();
                }else{
                    filters.add(pattern);
                    return null;
                }               
                
            }else if (operation.getOperator() == Ops.COL_IS_EMPTY){
                operation = (Operation<?>) ExpressionUtils.eq(operation.getArg(0), new ConstantImpl(RDF.nil));
            
            }else if (operation.getOperator() == Ops.CONTAINS_KEY){
                Path<?> path = (Path<?>) operation.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();
                Expression<?> key = transform(operation.getArg(1), filters);
                Block pattern = Blocks.pattern(transform(path, filters), mappedProperty.getKeyPredicate(), key);
                if (inNegation() || inOptionalPath()){
                    return pattern.exists();
                }else{
                    filters.add(pattern);
                    return null;
                }
                
            }else if (operation.getOperator() == Ops.CONTAINS_VALUE){
                Path<?> path = (Path<?>) operation.getArg(0);
                MappedPath mappedPath = getMappedPath(path);
                MappedProperty mappedProperty = mappedPath.getMappedProperty();
                
                if (mappedProperty.getValuePredicate() != null){
                    Expression<?> value = transform(operation.getArg(1), filters);
                    Block pattern = Blocks.pattern(transform(path, filters), mappedProperty.getValuePredicate(), value);
                    if (inNegation() || inOptionalPath()){
                        return pattern.exists();
                    }else{
                        filters.add(pattern);
                        return null;
                    }
                    
                }else{
                    operation = (Operation<?>) ExpressionUtils.eq((Path)path, operation.getArg(1));
                }                
                
            }else if (operation.getOperator() == Ops.MAP_IS_EMPTY){
                return new PredicateOperation(Ops.IS_NULL, transform(operation.getArg(0), filters));
                
            }else if (operation.getOperator() == Ops.COALESCE){
                List<Expression<?>> elements = new ArrayList<Expression<?>>();                
                operation = new OperationImpl(operation.getType(), Ops.COALESCE, transformList(operation.getArg(0), elements));
            }
                
            for (Expression<?> arg : operation.getArgs()){
                Expression<?> transformed = transform(arg, filters);
                if (transformed != null){
                    args.add(transformed);
                }else{
                    System.err.println(arg + " skipped");
                }
            }
        }finally{
            operatorStack.pop();
            if (!outerOptional && innerOptional){
                if (!filtersInOptional){
                    filters.endOptional();
                }
                pathToMapped = origPathToMapped;
            }
        }
                
        if (operation.getType().equals(Boolean.class)){
            return BooleanOperation.create((Operator)operation.getOperator(), args.toArray(new Expression[args.size()]));
        }else{
            return new OperationImpl(operation.getType(),operation.getOperator(), args);
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
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Expression<?> visit(Path<?> path, Filters filters){
        if (pathToMapped.containsKey(path)){
            return pathToMapped.get(path);
            
        }else if (path.getMetadata().getParent() != null){
            PathMetadata<?> md = path.getMetadata();
            PathType pathType = md.getPathType();
            Path<?> parent = (Path<?>) visit(md.getParent(), filters);
            Path<?> pathNode = null;
            Path<?> rdfPath = pathToMapped.get(path);
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
                            pathNode = var(path.accept(ToStringVisitor.DEFAULT, TEMPLATES));
                            varNames.disallow(path.toString());
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
                int index = ((Constant<Integer>)md.getExpression()).getConstant().intValue();
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
                    Expression<?> expr = transform(path.getMetadata().getExpression(), filters);
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
            f.add(handleRootPath((Path<?>) join.getTarget()));
        }            
        // where
        if (md.getWhere() != null){
            f.add(transform(f, md.getWhere()));
        } 
        // select
        if (!md.getProjection().isEmpty()){
            QueryMetadata rv = new DefaultQueryMetadata();
            for (Expression<?> e : md.getProjection()){
                rv.addProjection(transform(e, filters));
            }
            rv.addWhere(f.asBlock());
            return new SubQueryExpressionImpl<Object>(Object.class, rv);
        }else{
            return f.asBlock();    
        }
    }

    @SuppressWarnings("unchecked")
    public Expression<?> visit(TemplateExpression<?> template, Filters filters){
        List<Expression<?>> args = new ArrayList<Expression<?>>(template.getArgs().size());
        for (Expression<?> arg : args){
            Expression<?> transformed = transform(arg, filters);
            if (transformed != null){
                args.add(transformed);
            }
        }
        if (template.getType().equals(Boolean.class)){
            return new BooleanTemplate(template.getTemplate(), args);
        }else{
            return new TemplateExpressionImpl(template.getType(), template.getTemplate(), args);    
        }        
    }
    
    

}
